#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

require 'java'

# Configuration

# Edit this if the MicroTESK JAR is located elsewhere
$MICROTESK_JAR = "./dist/models.jar"
require $MICROTESK_JAR

if(ARGV.count < 2)
  abort "Arguments required: model package, template file"
end

classname = ARGV.shift

# Build MicroTESK, compile MicroTESK, build MicroTESK and set the CPU model class
puts "Creating model object"
#java_import Java::Ru.ispras.microtesk.model.arm.Model
java_import "ru.ispras.microtesk.model." + classname + ".Model"
#puts "If you don't see the next message, it means Java doesn't want to create a Model object?.. Seems to happen with ARM model, but not simple model"
$model = Model.new
puts "Model object created"

# Launcher

$working_directory = Dir.pwd
require_relative "src/ru/ispras/microtesk/templates/parse_templates.rb"