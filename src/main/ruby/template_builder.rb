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

def self.build_template_class(j_metamodel)
    
  # Initializing global variables
  $defined_situations = Set.new
  @registered_modes   = Hash.new

  instructions = j_metamodel.getInstructions()
  instructions.each do |instruction|
    define_instruction instruction
  end
end

private

# 
# Constants for Error Messages
# 

ERR_WRONG_MODE_ARG_NUMBER = 
  "%s\nDescription: wrong arguments number for addressing mode '%s': %d while %d is expected"

ERR_WRONG_MODE_ARG =
  "%s\nDescription: wrong argument '%s' for addressing mode '%s'"

ERR_WRONG_INST_ARG_NUMBER =
  "%s\nDescription: wrong number of arguments for instruction '%s': %d while %d is expected" 

# ---------------------------------------------------------------------------------------------- #
# 
# ---------------------------------------------------------------------------------------------- #

def define_instruction(i)

  inst_name = i.getName.to_s
  #puts "Processing #{inst_name}..."

  # Methods for test situations (added to the Instruction class)
  situations = i.getSituations
  situations.each do |situation|
    define_situation situation
  end

  # -------------------------------------------------------------------------------------------------------------- #
  # Generating convenient shortcuts for addressing modes                                                           #
  # -------------------------------------------------------------------------------------------------------------- #

  modes_for_args = Hash.new

  inst_arguments = i.getArguments
  inst_arguments.each_with_index do |arg, index|

    modes_for_args[index] = Array.new

      modes = arg.getAddressingModes()
      modes.each do |m|

      mode_name = m.getName().to_s
      modes_for_args[index].push mode_name

      # Make sure we didn't add this mode before and then add it
      if(!@registered_modes.has_key?(mode_name))
         #puts "Defining mode #{mode_name}..."

         mode_arg_names = m.getArgumentNames().to_a
         @registered_modes[mode_name] = mode_arg_names

         p = lambda do |*arguments|
           arg = Argument.new
           arg.mode = mode_name

           if arguments.first.is_a?(Integer) or arguments.first.is_a?(String) or arguments.first.is_a?(NoValue)

             if arguments.count != mode_arg_names.count
               raise MTRubyError, ERR_WRONG_MODE_ARG_NUMBER % [caller[0], mode_name, arguments.count, mode_arg_names.count]
             end

             arg.values = {}
             arguments.each_with_index do |n, ind|
               arg.values[mode_arg_names[ind]] = n
             end

           elsif arguments.first.is_a?(Hash)

             argumentss = arguments.first
             if(argumentss.count != mode_arg_names.count)
               raise MTRubyError, ERR_WRONG_MODE_ARG_NUMBER % [caller[0], mode_name, argumentss.count, mode_arg_names.count]
             end

             argumentss.keys.each do |n|
               if(!mode_arg_names.include?(n.to_s))
                 raise MTRubyError, ERR_WRONG_MODE_ARG % [caller[0], n, mode_name]
               end
             end

             arg.values = argumentss
           end

           return arg
         end

         define_method_for Template, mode_name, "mode", p
       end
     end

   end

   # -------------------------------------------------------------------------------------------------------------- #
   # Generating convenient shortcuts for instructions                                                               #
   # -------------------------------------------------------------------------------------------------------------- #

  p = lambda do  |*arguments, &situations|

    inst = Instruction.new
    inst.name = inst_name

    if(inst_arguments.count != arguments.count)
      raise MTRubyError, ERR_WRONG_INST_ARG_NUMBER % [caller[0], inst_name, arguments.count, inst_arguments.count]
    end

    arguments.each_with_index do |arg, ind|
        
      if arg.is_a? NoValue and !arg.is_immediate
        a = Argument.new
        a.mode = modes_for_args[ind].sample
    
        @registered_modes[a.mode].each do |mode|
          if arg.aug_value != nil
            a.values[mode] = arg.aug_value
          else
            a.values[mode] = NoValue.new
          end
        end
      else
        a = arg
      end

      # TODO: The situation/composite must come at the end of a block for this to work.
      # Is there a way to apply it if it's before the attributes?
      if situations != nil
        result = inst.instance_eval &situations
        inst.situation(result)
      end

      inst.arguments[inst_arguments[ind].getName()] = a

    end
    @instruction_receiver.receive(inst)
  end
     
  define_method_for Template, inst_name, "instruction", p
end
  
#
# Defines methods for test situations (added to the Instruction class)
#
def define_situation(situation)
  situation_name = situation.getName.to_s
  if $defined_situations.add?(situation_name)

    p = lambda do
      Situation.new(situation_name)
    end

    define_method_for Instruction, situation_name, "situation", p
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
