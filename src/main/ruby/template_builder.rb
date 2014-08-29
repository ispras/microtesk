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
  @registered_modes   = Hash.new

  modes = metamodel.getAddressingModes
  modes.each { |mode| define_addressing_mode mode }

  instructions = metamodel.getInstructions
  instructions.each { |instruction| define_instruction instruction }
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

#
# 
#
def define_instruction(i)

  inst_name = i.getName.to_s
  #puts "Defining instruction #{inst_name}..."

  # Methods for test situations (added to the Instruction class)
  situations = i.getSituations
  situations.each { |situation| define_situation situation }

  #
  # Generating convenient shortcuts for addressing modes
  #

  modes_for_args = Hash.new

  inst_arguments = i.getArguments.to_a
  inst_arguments.each_with_index do |arg, index|

    modes_for_args[index] = Array.new

    modes = arg.getTypeNames()
    modes.each do |m|
      mode_name = m.to_s
      modes_for_args[index].push mode_name
    end

  end

  #
  # Generating convenient shortcuts for instructions
  #

  p = lambda do |*arguments, &situations|
    # instruction = create_instruction inst_name, *arguments, &situations
    # @template.setRootOperation instruction
    # @template.endBuildingCall
    # return instruction 

    inst = Instruction.new
    inst.name = inst_name
    inst.block_id = @template.getCurrentBlockId

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

      inst.arguments[inst_arguments[ind].getName()] = a

    end

    # TODO: The situation/composite must come at the end of a block for this to work.
    # Is there a way to apply it if it's before the attributes?
    if situations != nil
      result = inst.instance_eval &situations
      inst.situation(result)
    end
    
    @instruction_receiver.receive(inst)
  end

  define_method_for Template, inst_name, "instruction", p
end

#
# Defines methods for addressing modes (added to the Template class)
# 
def define_addressing_mode(mode)

  mode_name = mode.getName().to_s
  mode_arg_names = mode.getArgumentNames().to_a

  #puts "Defining mode #{mode_name}..."

  @registered_modes[mode_name] = mode_arg_names

  p = lambda do |*arguments|
    # return create_addressing_mode mode_name, *arguments 

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

def create_addressing_mode(name, *args)
  builder = @template.newAddressingModeBuilder name
  build_primitive builder, args
end

def create_instruction(name, *args, &situations)
  builder = @template.newInstructionBuilder name

  if situations != nil
    situation = (Instruction.new).instance_eval &situations
    @template.setSituation situation.name
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
  java_import Java::Ru.ispras.microtesk.test.template.RandomValueBuilder

  labelIndex = 0
  args.each_pair do |name, value|

    if value.is_a? String or value.is_a? Symbol
      labelName = value.to_s
      value = labelIndex
      labelIndex = labelIndex + 1
      @template.addLabelReference labelName, name, value
    elsif value.is_a? RandomValueBuilder
      value = value.build
    end

    builder.setArgument name, value
  end
end

def set_arguments_from_array(builder, args)
  java_import Java::Ru.ispras.microtesk.test.template.RandomValueBuilder

  labelIndex = 0
  args.each do |value|

    labelName = nil
    if value.is_a? String or value.is_a? Symbol
      labelName = value.to_s 
      value = labelIndex
      labelIndex = labelIndex + 1
    elsif value.is_a? RandomValueBuilder
      value = value.build    
    end

    name = builder.addArgument value

    if nil != labelName
      @template.addLabelReference labelName, name, value
    end
  end
end

end # TemplateBuilder
