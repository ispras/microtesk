#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Launcher file         #
#                              #

require 'pathname'

if ARGV.count < 1
  abort "MTRuby argument format: <input template> [<output file>]"
end


if $working_directory != nil
  $working_directory = Dir.pwd
end

file = ARGV[0]
if !(Pathname.new file).absolute?
  file = $working_directory + "/" + file
end

if ARGV.count > 1
  output = [ARGV[1]]

  if !(Pathname.new output).absolute?
    output = $working_directory + "/" + output
  end

else
  output = nil
end



# This version requires JRuby. CRuby version pending...
require 'java'

# Reading configuration file (config switcher script pending...)
require_relative 'config'

# TemplateBuilder class that will initialize Template class with model data
require_relative 'lib/template_builder'
include TemplateBuilder

if $MICROTESK_JAR == nil
  $MICROTESK_JAR = "../../../../../dist/microtesk.jar"
end

# Initialize the MicroTESK Java library
require $MICROTESK_JAR

java_import Java::Ru.ispras.microtesk.model.samples.simple.Model
model = Model.new()

TemplateBuilder::build_template_class(model)

# Launch all executable templates

$template_classes = Array.new

# <TODO>
# For each inherited template that is_executable, require it
# and Template#parse

# file = "templates/demo_template.rb"

if File.file?(file)
  require file
else
  puts "MTRuby: warning: File '" + file.to_s + "' doesn't exist."
end

  $template_classes.each do |template_class|
  begin
    template = template_class.new (model)
    if template.is_executable
      puts "Parsing '" +
           File.basename(template_class.instance_method(:run).source_location.first) +
           "' ..."
      template.parse
      template.execute(model.getSimulator())
      template.output(output)
    end
  rescue Exception
    puts $!
  end


end