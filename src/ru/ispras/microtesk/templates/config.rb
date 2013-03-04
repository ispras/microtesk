#                              #
#   MicroTESK Ruby front-end   #
#                              #
#      Configuration file      #
#                              #

# ---------- Start ----------- #

#    Microtesk JAR location

#$MICROTESK_JAR = "../../../../../dist/microtesk.jar"

##    Sim-nml CPU description location
## TODO: this isn't really used yet
#
#$TEMPLATE_LOCATION = "../model/samples/simple/design.nml"
#
##    Template file/folder locations
## TODO: make a separate one for recursive folder inclusion
#
#$TEMPLATE_FILES = [
#    "#{File.dirname(__FILE__)}/templates/demo_template.rb",
#]
#
##    Generated test file output locations
##    (if fewer than template locations, the last one is used
##     for the remaining templates)
#
#$OUTPUT_LOCATIONS = [
#    "./bin"
#]

# ---- Debugging features ---- #

#    Write results to stdout?

$TO_STDOUT = true

#    Write results to files?

$TO_FILES = true

# ------------ End ----------- #

