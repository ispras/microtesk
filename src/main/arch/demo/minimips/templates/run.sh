#!/bin/bash
sh $MICROTESK_HOME/bin/generate.sh minimips $1.rb \
   --code-file-prefix $1 \
   --code-file-extension s \
   --tracer-log \
   --coverage-log \
   -v -sd 1>$1.stdout 2>$1.stderr
