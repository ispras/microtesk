#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Launcher file         #
#                              #

class MTRubyError < StandardError
  def initialize(msg = "You've triggered an MTRuby Error. TODO: avoid these situations and print stack trace")
    super
  end
end

require 'pathname'

if ARGV.count < 1
  abort "MTRuby argument format: <input template> [<output file>]"
end


if $working_directory == nil
  $working_directory = Dir.pwd
end

file = ARGV[0]
if !(Pathname.new file).absolute?
  file = $working_directory + "/" + file
end

if ARGV.count > 1
  output = ARGV[1]
  
  puts "|" + ARGV[0] + "|"
  puts "|" + output + "|"

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
#require_relative 'lib/template'
require_relative 'lib/template_builder'
include TemplateBuilder

# Initialize the MicroTESK Java library

if $MICROTESK_JAR == nil
  $MICROTESK_JAR = "../../jars/models.jar"
  require $MICROTESK_JAR
end

if $model == nil
  java_import Java::Ru.ispras.microtesk.model.samples.simple.Model
  model = Model.new()
else
  model = $model
end

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
    template = template_class.new
    template.set_model(model)
    
    if template.is_executable
      puts "Parsing '" +
           File.basename(template_class.instance_method(:run).source_location.first) +
           "' ..."
      puts
      template.parse
      template.execute#(model.getSimulator())
      template.output(output)
    end
  rescue Exception => e
  #  puts $!#.to_s + caller[0] + caller[1] + caller[2] + caller[3] #+ ": " + self.class.name
    if e.is_a?(MTRubyError)
      puts "#{e.class}:\n#{e.message}"
    end
    if e.respond_to?(:printStackTrace)
      e.printStackTrace
    end
    if !(e.is_a?(MTRubyError))
      raise e
    end
  end


end
