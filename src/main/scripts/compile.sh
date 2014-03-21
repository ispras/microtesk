#!/bin/sh

java -ea -jar "../lib/jars/microtesk.jar" -d "../output/src" $*
ant

