# Malic for compiler 2017

A compiler for the course [Compiler 2017](http://acm.sjtu.edu.cn/wiki/Compiler_2017) at ACM Class, SJTU. 

The source is a java-like language.
The target is x86-64 NASM.

With various optimizations, this compiler is ranked first in the performance competition of this course.

## Optimization
* Instruction selection
* Function inlining
* Control flow analysis
    * redundant jump elimination
* Dataflow analysis
    * common sub-expression elimination
    * constant propagation and folding
    * dead code elimination
* Register Allocation
    * a full implementation of George, Lal; Appel, Andrew W. (May 1996). *"Iterated Register Coalescing“*

for more details, please refer to [my report](doc/report.pdf)

## Build
bash build.bash

## Usage
```
Usage: java -jar Malic.jar [options]
Options:
  -in   <file> : M* language source code
  -out  <file> : x86-64 NASM output
  -help        : print this help page
```
