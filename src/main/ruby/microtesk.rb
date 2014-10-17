#
# Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require 'java'

require_relative 'config'
require_relative 'template'
require_relative 'utils'

module MicroTESK 
  
def self.main
  check_arguments
  check_tools

  puts "Home: " + HOME
  puts "Current directory: " + WD

  model = create_model ARGV[0]

  template_file = File.expand_path ARGV[1]
  puts "Template: " + template_file

  output_file = if ARGV.count > 2 then File.expand_path ARGV[2] else nil end
  if output_file then puts "Output file: " + output_file end

  Template.set_model model

  template_classes = prepare_template_classes(model, template_file)
  template_classes.each do |template_class, template_class_file|
    begin
      if template_class_file.eql?(template_file)
        puts "Processing #{template_class} defined in #{template_class_file}..." 
        template = template_class.new
        template.generate output_file
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

def self.check_tools

  if !File.exists?(TOOLS) || !File.directory?(TOOLS)
    abort "The '" + TOOLS + "' folder does not exist.\r\n" +
          "It stores external constraint solver engines and is required to generate constraint-based test data."
  end

end

def self.create_model(model_name)
  require MODELS_JAR
  require FORTRESS_JAR
  require TESTBASE_JAR
  require MICROTESK_JAR

  model_class_name = sprintf(MODEL_CLASS_FRMT, model_name)

  printf("Creating the %s model object (%s)...\r\n", model_name, model_class_name) 
  java_import model_class_name

  model = Model.new
  puts "Model object created"
  model
end

def self.prepare_template_classes(model, template_file)

  if File.file?(template_file)
    ENV['TEMPLATE'] = TEMPLATE
    require template_file
  else
    printf "MTRuby: warning: The %s file does not exist.\r\n", template_file
  end

  Template::template_classes
end

end # MicroTESK

MicroTESK.main
