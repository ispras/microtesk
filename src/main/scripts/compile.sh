#!/bin/sh

java -ea -jar "../libs/jars/microtesk.jar" -d "../output/src" $*
ant

