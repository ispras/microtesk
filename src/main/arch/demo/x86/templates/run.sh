#!/bin/bash
sh $MICROTESK_HOME/bin/generate.sh x86 $1.rb --code-file-prefix $1 --code-file-extension s -v -sd --ri X86 1>$1.stdout 2>$1.stderr
