#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Launcher file         #
#                              #

# This version requires JRuby. CRuby version pending...
require 'java'

# Reading configuration file (config switcher script pending...)
require_relative 'config'

# TemplateBuilder class that will initialize Template class with model data
require_relative 'lib/template_builder'
include TemplateBuilder

# Initialize the MicroTESK Java library
require $MICROTESK_JAR

# <TODO>
# theoretically this line should activate MicroTESK with an
# appropriate CPU description taken from the config
# ...or return a model based on a given CPU description?..

java_import Java::Ru.ispras.microtesk.model.samples.simple.Model

model = Model.new()

# Fill the template class with data from the active model in MicroTESK

TemplateBuilder::build_template_class(model)

# Launch all executable templates

$template_classes = Array.new

# <TODO>
# For each inherited template that is_executable, require it
# and Template#parse

$TEMPLATE_FILES.each_with_index do |file, i|
  if File.directory?(file)
    dir = file
    if file[file.length-1] != '/'
      dir += '/'
      puts dir
    end
    Dir[dir + "*.rb"].each {|file_req| require file_req }
  elsif File.file?(file)
    require file
  else
    puts "MTRuby: warning: File or directory '" + file.to_s + "' doesn't exist."
  end

  $template_classes.each do |template_class|
  begin
    template = template_class.new
    if template.is_executable
      puts "Parsing '" +
           File.basename(template_class.instance_method(:run).source_location.first) +
           "' ..."
      template.parse
      template.output $OUTPUT_LOCATIONS[[i, $OUTPUT_LOCATIONS.size - 1].min]
    end
  rescue Exception
    puts $!
  end
  end


end















# misc temp stuff

# Getting appropriate class names from template filenames
# template_class_names = Array.new

#$TEMPLATE_FILES.each do |templatePath|
#  template_path_elements = templatePath.split('/')
#  template_path_elements.last[0] = template_path_elements.last[0].upcase
#  template_class_names.push(
#      template_path_elements.last.split('.').first.gsub(/_./) {
#          |match| match[1].upcase} )
#end
