#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Launcher script        #
#                              #

require 'java'
require "pathname"
require_relative 'templates/config'
require MODELS_JAR

require_relative 'templates/lib/template_builder'
require_relative 'templates/lib/template'

include TemplateBuilder

class MTRubyError < StandardError
  def initialize(msg = "You've triggered an MTRuby Error. TODO: avoid these situations and print stack trace")
    super
  end
end

module MicroTESK 

WD = Dir.pwd

def self.main
  check_arguments
  model = create_model

  template_file = get_full_name(ARGV[1])
  puts "Template: " + template_file

  output_file = if ARGV.count > 2 then get_full_name(ARGV[2]) else nil end
  if output_file then puts "Output file: " + output_file end

  TemplateBuilder.build_template_class(model)

  require_template_file(template_file)
  template_classes = Template::template_classes

  template_classes.each do |template_class|
    begin
      template = template_class.new
      template.set_model(model)

      if template.is_executable
        printf "Translating %s...\r\n", get_template_file_name(template_class)

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

def self.create_model
  model_name = ARGV[0]
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

def self.require_template_file(template_file)
  if File.file?(template_file)
    require template_file
  else
    puts "MTRuby: warning: File '" + template_file + "' doesn't exist."
  end
end

def self.get_template_file_name(template_class)
  File.basename(template_class.instance_method(:run).source_location.first)
end

end # MicroTESK

MicroTESK.main
