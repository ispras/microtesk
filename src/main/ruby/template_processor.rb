#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

require 'java'
require "pathname"
require_relative 'config'
require MODELS_JAR

require_relative 'lib/template_builder'
require_relative 'lib/template'

include TemplateBuilder

class MTRubyError < StandardError
  def initialize(msg = "You've triggered an MTRuby Error. TODO: avoid these situations and print stack trace")
    super
  end
end

module MTRuby 

WD = Dir.pwd

def self.main
  check_arguments
  model = create_model(ARGV[0])

  template_file = get_full_name(ARGV[1])
  puts "Template: " + template_file

  output_file = if ARGV.count > 2 then get_full_name(ARGV[2]) else nil end
  if output_file then puts "Output file: " + output_file end

  template_classes = prepare_template_classes(model, template_file)
  template_classes.each do |template_class|
    begin
      template = template_class.new
      template.set_model(model)

      if template.is_executable
        printf "Processing %s...\r\n", get_template_file_name(template_class)

        template.parse
        template.execute
        template.output(output_file)
      end
    rescue Exception => e
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

end

def self.check_arguments
  if ARGV.count < 2
    abort "Wrong number of arguments. At least two are required.\r\n" + 
          "Argument format: <model name>, <template file>[, <output file>]"
  end
end

def self.create_model(model_name)
  model_class_name = sprintf(MODEL_CLASS_FRMT, model_name)

  printf("Creating the %s model object (%s)...\r\n", model_name, model_class_name) 
  java_import model_class_name

  model = Model.new
  puts "Model object created"
  model
end

def self.get_full_name(file)
  if (Pathname.new file).absolute? then file else File.join(WD, file) end
end

def self.prepare_template_classes(model, template_file)
  TemplateBuilder.build_template_class(model)

  if File.file?(template_file)
    require template_file
  else
    printf "MTRuby: warning: The %s file does not exist.\r\n", template_file
  end

  Template::template_classes
end

def self.get_template_file_name(template_class)
  File.basename(template_class.instance_method(:run).source_location.first)
end

end # MTRuby

MTRuby.main
