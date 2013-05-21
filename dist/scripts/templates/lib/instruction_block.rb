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
    @items.add item
  end

  def label(label)
    #@labels[label]
    @items.add label.to_s
    @labels[label.to_s] = @block_id
  end

  # Block construction
  def build(j_block_builder_factory, labels = Hash.new)

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

    was_block = false

    # Semantics of label inclusion:
    # The instruction attributes generally contain labels that _follow_ the instruction ("f_label")
    # Unless explicitly specified ("b_label") in case of there being no instructions

    delayed_labels = Array.new
    delayed_instruction = nil

    @items.each do |item|

      # If item.is_a? Label
      if item.is_a? String or item.is_a? Symbol
        if delayed_instruction == nil
          delayed_labels.push item
        else
          if !delayed_instruction.attributes.has_key? "b_label"
            delayed_instruction.attributes["b_label"] = Array.new
          end
          delayed_instruction.attributes["b_label"].push [item.to_s, @block_id]
        end
      else
        if delayed_instruction != nil
          if !delayed_instruction.attributes.has_key? "f_label"
            delayed_instruction.attributes["f_label"] = Array.new
          end
          delayed_labels.each do |i_item|
            delayed_instruction.attributes["f_label"].push [i_item.to_s, @block_id]
          end
        end
        delayed_labels.clear
      end

      # Now that we have all of the associated labels - build instruction
      if delayed_instruction != nil
        j_block_builder.addCall delayed_instruction.build, labels.merge(@labels)
        delayed_instruction = nil
      end

      if item.is_a? InstructionBlock
        j_block_builder.addBlock(item.build j_block_builder_factory, labels.merge(@labels))
      elsif item.is_a? Instruction
        # Delay instruction to gather labels
        delayed_instruction = item
      end
    end

    if delayed_instruction != nil
      j_block_builder.addCall delayed_instruction.build, labels.merge(@labels)
    end

    block = j_block_builder.build()

    block.getIterator().init()

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