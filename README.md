pl
==

Compiler of the ServalCJ programming language

This compiler is developed as an extension of abc: the AspectBench compiler for AspectJ.
http://www.sable.mcgill.ca/abc/
The version 1.3.0 is required to build the ServalCJ compiler.

To build the ServalCJ compiler, follow the instructions:
(Apache Ant and JDK are required; I only tested ant-1.9.3 and JDK1.6.1_45)

1. download and extract abc-1.3.0 (including the source code)
2. copy the source directory of the ServalCJ compiler under the directory abc-1.3.0/
3. cd abc-1.3.0/javanese
4. build the compiler using ant

Then, the following command will run the ServalCJ compiler

java -classpath $CLASSPATH_TO_ABC:$PATH_TO_ABC_SOURCE/javanese/classes abc.main.Main -ext abc.ja.javanese _source_files_of_ServalCJ_program_
