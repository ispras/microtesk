#!/bin/bash
sh $MICROTESK_HOME/bin/generate.sh x86nasm $1.rb --code-file-prefix $1 --code-file-extension s -v -sd --ri I80386 1>$1.stdout 2>$1.stderr
