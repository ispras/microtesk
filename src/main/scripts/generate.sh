#!/bin/sh
java -Xmx1024m -jar $MICROTESK_HOME/lib/jars/jruby.jar $MICROTESK_HOME/lib/ruby/microtesk.rb $MICROTESK_HOME/lib/jars/models.jar $*
