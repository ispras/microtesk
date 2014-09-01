#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# template_builder.rb, Apr 17, 2014 4:03:42 PM Andrei Tatarnikov
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

require 'set'

require_relative 'template'
require_relative 'utils'

# TODO:
# make errors display file/line numbers (there's an example of that in the last version)

# TODO:
# try to make sense of all this metaprogramming, it's only this convoluted
# because of the way the DSL is supposed to work - injecting methods makes
# life and code sort of complicated

module TemplateBuilder

def self.define_runtime_methods(metamodel)
  # Initializing global variables
  $defined_situations = Set.new

  modes = metamodel.getAddressingModes
  modes.each { |mode| define_addressing_mode mode }

  instructions = metamodel.getInstructions
  instructions.each { |instruction| define_instruction instruction }
end

private

#
# Defines methods for instructions (added to the Template class)
# 
def define_instruction(i)
  inst_name = i.getName.to_s
  #puts "Defining instruction #{inst_name}..."

  situations = i.getSituations
  situations.each { |situation| define_situation situation }
 
  p = lambda do |*arguments, &situations|
    instruction = create_instruction inst_name, *arguments, &situations
    @template.setRootOperation instruction
    @template.endBuildingCall
    instruction 
  end

  define_method_for Template, inst_name, "instruction", p
end

#
# Defines methods for addressing modes (added to the Template class)
# 
def define_addressing_mode(mode)
  mode_name = mode.getName().to_s
  #puts "Defining mode #{mode_name}..."

  p = lambda do |*arguments|
    create_addressing_mode mode_name, *arguments 
  end

  define_method_for Template, mode_name, "mode", p
end

#
# Defines methods for test situations (added to the Template class)
#
def define_situation(situation)
  situation_name = situation.getName.to_s
  if $defined_situations.add?(situation_name)

    p = lambda do
      situation_name
    end

    define_method_for Template, situation_name, "situation", p
  end
end

def create_addressing_mode(name, *args)
  builder = @template.newAddressingModeBuilder name
  build_primitive builder, args
end

def create_instruction(name, *args, &situations)
  builder = @template.newInstructionBuilder name

  if situations != nil
    situation = self.instance_eval &situations
    @template.setSituation situation
  end

  build_primitive builder, args
end

def build_primitive(builder, args)
  if !args.is_a?(Array)
    raise MTRubyError, "Arguments must be stored in an array."
  end

  if args.count == 1 and args.first.is_a?(Hash)
    set_arguments_from_hash builder, args.first
  else
    set_arguments_from_array builder, args
  end

  builder.build
end

def set_arguments_from_hash(builder, args)
  labelIndex = 0
  args.each_pair do |name, value|

    if value.is_a? String or value.is_a? Symbol
      labelName = value.to_s
      value = labelIndex
      labelIndex = labelIndex + 1
      @template.addLabelReference labelName, name, value
    end

    builder.setArgument name.to_s, value
  end
end

def set_arguments_from_array(builder, args)
  labelIndex = 0
  args.each do |value|

    labelName = nil
    if value.is_a? String or value.is_a? Symbol
      labelName = value.to_s 
      value = labelIndex
      labelIndex = labelIndex + 1
    end

    name = builder.addArgument value

    if nil != labelName
      @template.addLabelReference labelName, name, value
    end
  end
end

# 
# Defines a method in the target class. If such method is already defined,
# the method type is added to the method name as a prefix to make the name unique.
# If this does not help, an error is reported.  
# 
# Parameters:
#   target_class Target class (Class object)
#   method_name  Method name (String)
#   method_type  Method type (String) 
#   method_body  Body for the method (Proc)
#
def define_method_for(target_class, method_name, method_type, method_body)

  method_name = method_name.downcase
  # puts "Defining method #{target_class}.#{method_name} (#{method_type})..."

  if !target_class.method_defined?(method_name)
    target_class.send(:define_method, method_name, method_body)
  elsif !target_class.method_defined?("#{method_type}_#{method_name}")
    target_class.send(:define_method, "#{method_type}_#{method_name}", method_body)
  else
    puts "Error: Failed to define the #{method_name} method (#{method_type})"
  end

end

end # TemplateBuilder
