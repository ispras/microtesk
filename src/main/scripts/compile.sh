#!/bin/sh

ant -f $MICROTESK_HOME/bin/build.xml clean
java -ea -jar $MICROTESK_HOME/lib/jars/microtesk.jar -d $MICROTESK_HOME/gen/src $*
ant -f $MICROTESK_HOME/bin/build.xml

