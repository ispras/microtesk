#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

# Configuration

# Edit this if the MicroTESK JAR is located elsewhere
$MICROTESK_JAR = "./dist/microtesk.jar"
require $MICROTESK_JAR

require 'fileutils'

# Build MicroTESK, compile MicroTESK, build MicroTESK and set the CPU model class
java_import Java::Ru.ispras.microtesk.model.arm.Model
#java_import Java::Ru.ispras.microtesk.model.samples.simple.Model
#puts "If you don't see the next message, it means Java doesn't want to create a Model object?.. Seems to happen with ARM model, but not simple model"
puts "Creating model object"
j_metamodel = Model.new
puts "Model object created"

# Launcher

md_code = []

md_code.push j_metamodel.class.name.split("::")[1] + " instruction set reference"
md_code.push "====================="
md_code.push ""

puts "Printing documentation"
$working_directory = Dir.pwd

    instructions = j_metamodel.getInstructions()
    instructions.each do |i|

      instruction_name = i.getName.to_s
      inst_arguments = i.getArguments

      md_code.push instruction_name
      md_code.push "---------------------"
      md_code.push "Arguments:"
      md_code.push ""

      arg_names = Array.new

      inst_arguments.each do |arg|
        str = "    " + arg.getName() + ": "
        modes = arg.getAddressingModes()
        modes.each_with_index do |m, ii|
          if(ii > 0)
            str += " | " end
          str += m.getName() + "("
          # Make sure we didn't add this mode before and then add it
            m.getArgumentNames().each_with_index do |n, iii|
              if(iii > 0)
                str += ", " end
              str += n
            end
          str += ")"

        end
        md_code.push str
      end
      md_code.push ""
    end

if(!File.directory? "./docs")
  FileUtils.mkdir_p("./docs")
end

File.open("./docs/" + j_metamodel.class.name.split("::")[1] + ".md", 'w') do |file|
  md_code.each do |s|
    file.puts s
  end
end
puts "Documentation printed to 'docs' folder"