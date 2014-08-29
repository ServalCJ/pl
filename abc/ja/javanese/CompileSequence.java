/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
 * Copyright (C) 2008 Torbjorn Ekman
 * Copyright (C) 2008 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.ja.javanese;

import abc.main.AbcExtension;
import abc.main.AbcTimer;
import abc.main.CompilerFailedException;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.DeclareParentsWeaver;
import abc.weaving.weaver.IntertypeAdjuster;

import java.util.*;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.StdErrorQueue;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.Scene;
import soot.SootMethod;
import abc.aspectj.visit.PatternMatcher;
import abc.ja.javanese.jrag.*;

import java.io.*;

public class CompileSequence extends abc.ja.CompileSequence {
    private String jvn_toSrc = "jvnoptions.toSrc";

  public CompileSequence(AbcExtension ext) {
    super(ext);
  }

  private void addError(Problem problem) {
    Position p;
    if(problem.column() != -1)
      p = new Position(problem.fileName(), problem.line(), problem.column());
    else
      p = new Position(problem.fileName(), problem.line());
    error_queue().enqueue(ErrorInfo.SEMANTIC_ERROR, problem.message(), p);
  }

  private void addWarning(Problem problem) {
	    Position p;
	    if(problem.column() != -1)
	      p = new Position(problem.fileName(), problem.line(), problem.column());
	    else
	      p = new Position(problem.fileName(), problem.line());
	    error_queue().enqueue(ErrorInfo.WARNING, problem.message(), p);
  }

  public ErrorQueue error_queue() {
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");
    return error_queue;
  }

  // throw CompilerFailedException if there are errors
  // place errors in error_queue
  public void compile() throws CompilerFailedException, IllegalArgumentException {
    error_queue = abcExt.getErrorQueue();
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");

    //DEBUG
    //abc.main.Debug.v().parserTrace = true;

    try {
      Collection c = new ArrayList();

      boolean toSrc = false;
      for (Iterator iter = aspect_sources.iterator(); iter.hasNext(); ) {
          String src = (String)iter.next();
	  if (src.equals(jvn_toSrc)) {
	      toSrc = true;
	  }
      }
      aspect_sources.remove(jvn_toSrc);

      c.addAll(aspect_sources);
      c.add("-classpath");
      c.add(OptionsParser.v().classpath());
      String[] args = new String[c.size()];
      int index = 0;
      for(Iterator iter = c.iterator(); iter.hasNext(); index++) {
        String s = (String)iter.next();
        args[index] = s;
      }
      Program program = new Program();
      ASTNode.reset();

      program.initBytecodeReader(new BytecodeParser());
      program.initJavaParser(
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new abc.ja.javanese.parse.JavaParser().parse(is, fileName, error_queue);
          }
        }
      );
      program.initOptions();
      program.addKeyValueOption("-classpath");
      program.addKeyOption("-verbose");
      program.addOptions(args);
      Collection files = program.files();

      for(Iterator iter = files.iterator(); iter.hasNext(); ) {
        String name = (String)iter.next();
        program.addSourceFile(name);
      }
      for(Iterator iter = jar_classes.iterator(); iter.hasNext(); ) {
    	  String name = (String)iter.next();
    	  CompilationUnit u = program.getCompilationUnit(name);
    	  u.weavableClass = true;
    	  program.addCompilationUnit(u);
      }
      for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
        CompilationUnit unit = (CompilationUnit)iter.next();
        if(unit.fromSource()) {
          // abort if there were syntax or lexical errors
          if(error_queue().errorCount() > 0)
            throw new CompilerFailedException("There were errors.");
        }
      }

      program.preprosessServalCJ();

      // ServalCJ error checking
      ArrayList errorsCJ = new ArrayList();
      program.errorCheckCJ(errorsCJ);
      if(!errorsCJ.isEmpty()) {
	  Collections.sort(errorsCJ);
	  for (Iterator iter = errorsCJ.iterator(); iter.hasNext(); ) {
	      Problem p = (Problem)iter.next();
	      addError(p);
	  }
	  throw new CompilerFailedException("There were errors.");
      }

      // performing translation from ServalCJ to AspectJ
      //      program.processAnonymousContexts();
      program.toAspectJ();
      if (toSrc) {
	  program.storeGeneratedSrc();
	  System.exit(0);
      }

      if(Program.verbose())
        System.out.println("Error checking");
      ArrayList errors = new ArrayList();
      ArrayList warnings = new ArrayList();

      program.errorCheck(errors, warnings);
      if(!errors.isEmpty()) {
        Collections.sort(errors);
        for(Iterator iter2 = errors.iterator(); iter2.hasNext(); ) {
          Problem p = (Problem)iter2.next();
          addError(p);
        }
        throw new CompilerFailedException("There were errors.");
      }
      if(!warnings.isEmpty()) {
          Collections.sort(warnings);
          for(Iterator iter2 = warnings.iterator(); iter2.hasNext(); ) {
            Problem p = (Problem)iter2.next();
            addWarning(p);
          }
      }

      program.generateIntertypeDecls();
      program.transformation();

      program.jimplify1();
      program.jimplify2();

      abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().buildAspectHierarchy();
      abc.main.AbcTimer.mark("Aspect inheritance");
      abc.main.Debug.phaseDebug("Aspect inheritance");

    } catch (Error /*polyglot.main.UsageError*/ e) {
      throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
    }

    // Output the aspect info
    if (abc.main.Debug.v().aspectInfo)
      abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().print(System.err);
  }

}
