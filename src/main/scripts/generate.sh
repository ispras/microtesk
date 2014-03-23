#!/bin/sh
java -Xmx1024m -jar $MICROTESK_HOME/lib/jars/jruby.jar $MICROTESK_HOME/lib/ruby/template_processor.rb $MICROTESK_HOME/lib/jars/models.jar $*
