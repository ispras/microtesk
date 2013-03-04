#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

# Configuration

# Edit this if the MicroTESK JAR is located elsewhere
$MICROTESK_JAR = "dist/microtesk.jar"

# Launcher

$working_directory = Dir.pwd
require_relative "src/ru/ispras/microtesk/templates/parse_templates.rb"