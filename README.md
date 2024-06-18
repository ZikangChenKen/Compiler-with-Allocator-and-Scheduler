# Compiler-with-Allocator-and-Scheduler
A compiler front end—for the intermediate representation, ILOC. (ILOC is described in Section 7 of this document.) The front end will read a file and determine if the file contains a syntactically correct ILOC block of ILOC code or not. The output that your front end produces will depend on the specific options specified on the command line that invoked it.

Built a local register allocator—that is, a program that reads in a single block of ILOC code, transforms that block so that it uses a specified number of registers, and writes the transformed block out to the standard output stream. The input and output blocks are both written in the ILOC subset.

Design, test, and implement an instruction scheduler that operates on a single basic block of ILOC code. The scheduler should rearrange the instructions in the input block to reduce the number of cycles required to execute the output block.
