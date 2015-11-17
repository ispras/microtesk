@call ant -f %MICROTESK_HOME%/bin/build.xml clean
@call java -ea -jar "%MICROTESK_HOME%/lib/jars/microtesk.jar" -od "%MICROTESK_HOME%/gen" %*
@call ant -f %MICROTESK_HOME%/bin/build.xml
