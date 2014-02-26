#!/bin/sh

java -ea -jar "dist/jars/microtesk.jar" $*
ant build_models

