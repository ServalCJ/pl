/*
 * Overriding advice weaving in case where both active and until
 * expressions in the same context are matched at the same shadow.
 *
 * Copyright (C) 2013 Tetsuo Kamina
 */

package abc.ja.javanese;

import java.util.Iterator;
import java.util.List;

import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.GlobalAspectInfo;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.matching.MethodPosition;
import abc.weaving.matching.ShadowMatch;
import abc.weaving.matching.ShadowType;
import abc.weaving.matching.WeavingEnv;
import abc.weaving.residues.AlwaysMatch;
import abc.weaving.residues.AndResidue;
import abc.weaving.residues.NeverMatch;
import abc.weaving.residues.Residue;
import polyglot.types.SemanticException;
import polyglot.util.InternalCompilerError;
import soot.SootClass;
import soot.SootMethod;

abstract class AdviceApplication extends abc.weaving.matching.AdviceApplication {

    public AdviceApplication(AbstractAdviceDecl advice, Residue residue) {
	super(advice,residue);
    }
    
    public static void doShadows(GlobalAspectInfo info,
				 MethodAdviceList mal,
				 SootClass cls,
				 SootMethod method,
				 MethodPosition pos)
	throws SemanticException {
	//	System.out.println("DEBUG: overriding doShadows");
	
	Iterator<ShadowType> shadowIt;
	for (shadowIt = abc.main.Main.v().getAbcExtension().shadowTypes();
	     shadowIt.hasNext(); ) {
	    ShadowType st = shadowIt.next();
	    ShadowMatch sm;
	    try {
		sm = st.matchesAt(pos);
	    } catch (InternalCompilerError e) {
		throw new InternalCompilerError
		    (e.message(),
		     e.position()==null
		     ? abc.polyglot.util.ErrorInfoFactory.getPosition(pos.getContainer(), pos.getHost())
		     : e.position(),
		     e.getCause());
	    } catch (Throwable e) {
		throw new InternalCompilerError
		    ("Error while looking for join point shadow",
		     abc.polyglot.util.ErrorInfoFactory.getPosition(pos.getContainer(), pos.getHost()),
		     e);
	    }
	    if (sm==null) continue;

	    Iterator<AbstractAdviceDecl> adviceIt;
	    /*
	    List<Pointcut> pcset;
	    List<Pointcut> flicSet;
	    for (adviceIt=info.getAdviceDecls().iterator();
		 adviceIt.hasNext(); ) {
	    }
	    */
	    for (adviceIt=info.getAdviceDecls().iterator();
		 adviceIt.hasNext(); ) {
		final AbstractAdviceDecl ad = adviceIt.next();

		try {
		    Pointcut pc = ad.getPointcut();
		    WeavingEnv we = ad.getWeavingEnv();

		    if (abc.main.Debug.v().showPointcutMatching) {
			System.out.println("Matching " + pc + " at " + sm);
		    }

		    // manual short-circuit logic
		    Residue residue = AlwaysMatch.v();

		    List<ResidueConjunct> conjuncts = abc.main.Main.v().getAbcExtension().residueConjuncts(ad,pc,sm,method,cls,we);

		    for (Iterator<ResidueConjunct> cit=conjuncts.iterator();
			 cit.hasNext(); ) {
			ResidueConjunct rc = cit.next();
			if (!NeverMatch.neverMatches(residue)) {
			    residue = AndResidue.construct(residue, rc.run());
			}
		    }

		    if (abc.main.Debug.v().showPointcutMatching
			&& !NeverMatch.neverMatches(residue)) {
			System.out.println("residue: " + residue);
		    }
		    if (!NeverMatch.neverMatches(residue)) {
			System.out.println("DEBUG: " + sm.getClass().getName());
			sm.addAdviceApplication(mal,ad,residue);
		    }
		} catch (InternalCompilerError e) {
		    throw new InternalCompilerError
			(e.message(),
			 e.position()==null ? ad.getPosition() : e.position(),
			 e);
		} catch (Throwable e) {
		    throw new InternalCompilerError
			("Error during matching",
			 ad.getPosition(),
			 e);
		}
	    }
	    mal.flush();
	}
	//	abc.weaving.matching.AdviceApplication.doShadows(info,mal,cls,method,pos);
    }

}