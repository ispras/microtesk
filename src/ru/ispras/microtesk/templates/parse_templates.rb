#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Launcher file         #
#                              #

# TODO: For debug reasons it doesn't accept command line arguments yet, gonna get fixed at the next opportunity

# This version requires JRuby. CRuby version pending...
require 'java'

# Reading configuration file (config switcher script pending...)
require_relative 'config'

# TemplateBuilder class that will initialize Template class with model data
require_relative 'lib/template_builder'
include TemplateBuilder

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

file = "templates/demo_template.rb"

if File.file?(file)
  require file
else
  puts "MTRuby: warning: File '" + file.to_s + "' doesn't exist."
end

  $template_classes.each do |template_class|
  begin
    template = template_class.new
    if template.is_executable
      puts "Parsing '" +
           File.basename(template_class.instance_method(:run).source_location.first) +
           "' ..."
      template.parse
      template.execute(model.getSimulator())
      template.output("bin/demo.asm")
    end
  rescue Exception
    puts $!
  end


end