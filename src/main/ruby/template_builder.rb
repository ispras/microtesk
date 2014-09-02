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
    
  ops = metamodel.getOperations
  ops.each { |op| define_operation op }

  instructions = metamodel.getInstructions
  instructions.each { |instruction| define_instruction instruction }
end

private

#
# Defines methods for addressing modes (added to the Template class)
# 
def define_addressing_mode(mode)
  name = mode.getName().to_s
  #puts "Defining mode #{name}..."

  p = lambda do |*arguments|
    builder = @template.newAddressingModeBuilder name
    build_primitive builder, arguments
  end

  define_method_for Template, name, "mode", p
end

#
# Defines methods for operations (added to the Template class)
# 
def define_operation(op)
  name = op.getName().to_s
  #puts "Defining operation #{name}..."
end

#
# Defines methods for instructions (added to the Template class)
# 
def define_instruction(i)
  name = i.getName.to_s
  #puts "Defining instruction #{name}..."

  situations = i.getSituations
  situations.each { |situation| define_situation situation }

  p = lambda do |*arguments, &situations|
    builder = @template.newInstructionBuilder name
    instruction = build_primitive builder, arguments

    if situations != nil
      situation = self.instance_eval &situations
      @template.setSituation situation
    end

    @template.setRootOperation instruction
    @template.endBuildingCall
  end

  define_method_for Template, name, "instruction", p
end

#
# Defines methods for test situations (added to the Template class)
#
def define_situation(situation)
  name = situation.getName.to_s
  if $defined_situations.add?(name)

    p = lambda do
      name
    end

    define_method_for Template, name, "situation", p
  end
end

def build_primitive(builder, args)
  if args.count == 1 and args.first.is_a?(Hash)
    set_arguments_from_hash builder, args.first
  else
    set_arguments_from_array builder, args
  end

  builder.build
end

def set_arguments_from_hash(builder, args)
  args.each_pair do |name, value|
    value = value.to_s if value.is_a? Symbol
    builder.setArgument name.to_s, value
  end
end

def set_arguments_from_array(builder, args)
  args.each do |value|
    value = value.to_s if value.is_a? Symbol
    builder.addArgument value
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
