#!/bin/sh

ant -f $MICROTESK_HOME/bin/build.xml clean
java -ea -jar $MICROTESK_HOME/lib/jars/microtesk.jar -od $MICROTESK_HOME/gen $*
ant -f $MICROTESK_HOME/bin/build.xml

