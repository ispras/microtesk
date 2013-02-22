#!/bin/sh

 ~casse/.local/powerpc-linux-gnu/gcc-2.95.3-glibc-2.2.2/bin/powerpc-linux-gnu-gcc -static  $1.c -o $1
 ~casse/.local/powerpc-linux-gnu/gcc-2.95.3-glibc-2.2.2/bin/powerpc-linux-gnu-objdump -d $1 > $1.dis
