#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

# Configuration

# Edit this if the MicroTESK JAR is located elsewhere
$MICROTESK_JAR = "./dist/microtesk.jar"
require $MICROTESK_JAR

# Build MicroTESK, compile MicroTESK, build MicroTESK and set the CPU model class
java_import Java::Ru.ispras.microtesk.model.arm.Model
#puts "If you don't see the next message, it means Java doesn't want to create a Model object?.. Seems to happen with ARM model, but not simple model"
$model = Model.new
#puts "Model object created"

# Launcher

$working_directory = Dir.pwd
require_relative "src/ru/ispras/microtesk/templates/parse_templates.rb"