#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# constructs.rb, Aug 4, 2014 5:30:19 PM Andrei Tatarnikov
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

require_relative "output"

#
# Description:
#
# The Argument class stores information about an instruction call argument.
# This information includes the name of the addressing mode being used 
# and arguments passed to the addressing mode.
#  
class Argument

  # Addressing mode name
  #
  attr_accessor :mode

  # Table of arguments of the addressing mode, where
  # key (String) is the argument name and value (Object) is the argument value
  #
  attr_accessor :values

  #
  # Initializes a new instance of an Argument object.
  #
  def initialize
    @mode   = "UntitledMode"
    @values = Hash.new
  end

  #
  # Creates an instance of a Java Argument object that is used in 
  # AbstractCall object.
  #
  # Parameters:
  #   j_arg_builder A Java ArgumentBuilder object
  #   (ru.ispras.microtesk.test.block.ArgumentBuilder) that creates
  #   a Java Argument object  
  #
  # Returns:
  #   A new Java Argument object (ru.ispras.microtesk.test.block.Argument)
  #
  def build(j_arg_builder)

    @values.each_pair do |key, value|
      if value.is_a? NoValue
        #  Handle NoValue
        j_arg_builder.setRandomArgument(key)
      else
        j_arg_builder.setArgument(key, value)
      end
    end

    j_arg_builder.build()
  end

end

# Instruction block class - May version
# This class is used to build blocks by means of the MicroTESK API

class InstructionBlock

  attr_accessor :name, :items, :attributes, :block_id

  @@block_id = 0

  # Mostly instance variables
  def initialize

    @name = ""

    # Array of Instruction and InstructionBlock
    @items = Array.new

    # String -> Integer
    @labels = Hash.new

    @attributes = Hash.new

    # Instruction iterators from the Test Engine
    #@sequences = Array.new

    @@block_id += 1

    # Id used to differ labels among blocks
    @block_id = @@block_id

  end

  def receive(item)
    if item.is_a? Instruction
      item.block_id = @block_id
    end
    @items.push item
  end

  def label(label)
    #@labels[label]
    @items.push label
    @labels[label.name] = @block_id
  end

  # Block construction
  def build(j_block_builder_factory, labels = Hash.new, stack = [])

    l_stack = stack + [@block_id]

    label_text_formatter = lambda do |name|
      text = name
      l_stack.each { |t| text += "_" + t.to_s }
      text
    end

    j_block_builder = j_block_builder_factory.newBlockBuilder()

    if @attributes.has_key? :compositor
      j_block_builder.setCompositor(@attributes[:compositor])
    end

    if @attributes.has_key? :combinator
      j_block_builder.setCombinator(@attributes[:combinator])
    end

    @attributes.each_pair do |key, value|
      j_block_builder.setAttribute(key.to_s, value)
    end

    # Semantics of label inclusion:
    # The instruction attributes generally contain labels that _follow_ the instruction ("f_label")
    # Unless explicitly specified ("b_label") in case of there being no instructions

    delayed_labels      = Array.new
    delayed_outputs     = Array.new
    delayed_instruction = nil    

    @items.each do |item|

      # LABELS 
      if item.is_a? Label
        if delayed_instruction == nil
          delayed_labels.push item.name
        else
          delayed_instruction.add_item_to_attribute "f_labels", [item.name, l_stack] #[item.to_s, @block_id]
          puts "Label " + label_text_formatter.call(item.name)
        end
      else
        if delayed_instruction != nil
          delayed_labels.each do |i_item|
            delayed_instruction.add_item_to_attribute "b_labels", [i_item, l_stack] #[i_item.to_s, @block_id]
            puts "Label " + label_text_formatter.call(i_item)
          end
          delayed_labels.clear
        end
      end

      # OUTPUT
      if item.is_a? Output::Output
        output = item.java_object
        if delayed_instruction == nil
          delayed_outputs.push output
        else
          if output.isRuntime
            delayed_instruction.add_item_to_attribute "f_runtime", output
          else
            delayed_instruction.add_item_to_attribute "f_output", output
          end
        end
      else
        if delayed_instruction != nil
          delayed_outputs.each do |i_item|
            if i_item.isRuntime
              delayed_instruction.add_item_to_attribute "b_runtime", i_item
            else
              delayed_instruction.add_item_to_attribute "b_output", i_item
            end
          end
          delayed_outputs.clear
        end
      end

      # Now that we have all of the associated labels - build instruction
      if delayed_instruction != nil
        j_block_builder.addCall delayed_instruction.build(j_block_builder_factory.newAbstractCallBuilder(delayed_instruction.name), labels.merge(@labels), l_stack)
        delayed_instruction = nil
      end

      if item.is_a? InstructionBlock
        j_block_builder.addBlock(item.build j_block_builder_factory, labels.merge(@labels), l_stack)
        delayed_instruction = nil
      end
      
      if item.is_a? Instruction
        # Delay instruction to gather labels
        delayed_instruction = item
      end
    end

    if delayed_instruction != nil
      j_block_builder.addCall delayed_instruction.build(j_block_builder_factory.newAbstractCallBuilder(delayed_instruction.name), labels.merge(@labels), l_stack)
    end

    block = j_block_builder.build()

    #block.getIterator().init()

    #while block.getIterator.hasValue()
    #  seq = block.getIterator().value()
    #
    #  seq.each do |call|
    #
    #
    #    @@sequence_id += 1
    #  end
    #
    #  block.getIterator().next()
    #end

    block

  end

end

class Instruction

  attr_accessor :attributes, :arguments, :name, :situation, :block_id

  # Mostly instance variables
  def initialize

    @name = "UntitledInstruction"

    @arguments = Hash.new
    @attributes = Hash.new
    @labels = Array.new

    @situation = nil

    @block_id = 0

  end

  def situation(s)
    @situation = s
  end

  def attribute(key, value)
    @attributes[key] = value
  end

  def add_item_to_attribute(attr_name, item)
    if !@attributes.has_key? attr_name
      @attributes[attr_name] = Array.new
    end
    @attributes[attr_name].push item
  end

  # Instruction construction
  def build(j_instruction_builder, labels, stack)

    @attributes["labels"] = Array.new
    # @labels.each do |label|
    #   @attributes["labels"].push [label, labels[label]]
    # end

    @attributes.each_pair do |key, value|
      j_instruction_builder.setAttribute(key, value)
    end

    @arguments.each_pair do |name, value|
      if value.is_a? String or value.is_a? Symbol
        j_instruction_builder.setArgumentImmediate(name, 0)
        @attributes["labels"].push [[value.to_s, stack], name]
      elsif value.is_a? Integer
        j_instruction_builder.setArgumentImmediate(name, value)
      elsif value.is_a? Argument
        value.build j_instruction_builder.setArgumentUsingBuilder(name, value.mode)
      elsif value.is_a? NoValue
        if value.is_immediate
          j_instruction_builder.setArgumentImmediateRandom(name)
        else
          puts "Ruby-TDL warning: unexpected random argument behaviour"
          # Should this even happen?
        end
      end
    end

    # Logic?..
    #s_b =
    # TODO: need API to build a logical op tree of situations
    if(@situation != nil)
      j_instruction_builder.setTestSituation(@situation.name).build()
    end
    #s_b.setSituation(s_b.build)

      j_instruction_builder.build()
      
  end


end


class Attribute

  attr_accessor :name, :parameters

  def initialize

    @name = "UntitledAttribute"

    # String -> Object
    @parameters = Hash.new

  end

end

class Label
  # Returns the Java object Label.
  attr_reader :java_object

  def initialize(java_object)
    @java_object = java_object
  end

  def name
    @java_object.getName.to_s
  end
end

class NoValue

  attr_accessor :aug_value, :is_immediate

  def initialize(aug_value = nil)

    @aug_value = aug_value
    @is_immediate = false

  end

end

class Situation

  attr_accessor :name

  def initialize(name = "NoSituation")
    @name = name
  end

end
