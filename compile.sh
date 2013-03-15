#!/bin/sh

java -ea -jar "dist/microtesk.jar" $*
ant build_models

