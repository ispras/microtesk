@call ant -f %MICROTESK_HOME%/bin/build.xml clean
@call java -ea -jar "%MICROTESK_HOME%/lib/jars/microtesk.jar" -d "%MICROTESK_HOME%/gen/src" %*
@call ant -f %MICROTESK_HOME%/bin/build.xml
