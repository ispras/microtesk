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
    delayed_outdebugs = Array.new
    delayed_debugs = Array.new
    delayed_outstrings = Array.new
    delayed_instruction = nil

    @items.each do |item|

      # LABELS 
      if item.is_a? Label
        if delayed_instruction == nil
          delayed_labels.push item.name
        else
          if !delayed_instruction.attributes.has_key? "f_label"
            delayed_instruction.attributes["f_labels"] = Array.new
          end
          delayed_instruction.attributes["f_labels"].push [item.name, l_stack] #[item.to_s, @block_id]

          text = item.name
          l_stack.each do |t|
            text += "_" + t.to_s
          end
          puts "Label " + text
          
        end
      else
        if delayed_instruction != nil
          if !delayed_instruction.attributes.has_key? "b_label"
            delayed_instruction.attributes["b_labels"] = Array.new
          end
          delayed_labels.each do |i_item|
            delayed_instruction.attributes["b_labels"].push [i_item, l_stack] #[i_item.to_s, @block_id]

            text = i_item
            l_stack.each do |t|
              text += "_" + t.to_s
            end
            puts "Label " + text
            
          end
          delayed_labels.clear
        end
      end
      
      # OUTPUT DEBUG
      
      if item.is_a? OutputDebug
        if delayed_instruction == nil
          delayed_outdebugs.push item.proc
        else
          if !delayed_instruction.attributes.has_key? "f_output_debug"
            delayed_instruction.attributes["f_output_debug"] = Array.new
          end
          delayed_instruction.attributes["f_output_debug"].push item.proc
        end
      else
        if delayed_instruction != nil
          if !delayed_instruction.attributes.has_key? "b_output_debug"
            delayed_instruction.attributes["b_output_debug"] = Array.new
          end
          delayed_outdebugs.each do |i_item|
            delayed_instruction.attributes["b_output_debug"].push i_item
          end
          delayed_outdebugs.clear
        end
      end
      
      # RUNTIME DEBUG
      
      if item.is_a? RuntimeDebug
        if delayed_instruction == nil
          delayed_debugs.push item.proc
        else
          if !delayed_instruction.attributes.has_key? "f_runtime_debug"
            delayed_instruction.attributes["f_runtime_debug"] = Array.new
          end
          delayed_instruction.attributes["f_runtime_debug"].push item.proc
        end
      else
        if delayed_instruction != nil
          if !delayed_instruction.attributes.has_key? "b_runtime_debug"
            delayed_instruction.attributes["b_runtime_debug"] = Array.new
          end
          delayed_debugs.each do |i_item|
            delayed_instruction.attributes["b_runtime_debug"].push i_item
          end
          delayed_debugs.clear
        end
      end

      
      # OUTPUT STRING

      if item.is_a? OutputDebug
        if delayed_instruction == nil
          delayed_outstrings.push item.proc
        else
          if !delayed_instruction.attributes.has_key? "f_output_string"
            delayed_instruction.attributes["f_output_string"] = Array.new
          end
          delayed_instruction.attributes["f_output_string"].push item.text
        end
      else
        if delayed_instruction != nil
          if !delayed_instruction.attributes.has_key? "b_output_string"
            delayed_instruction.attributes["b_output_string"] = Array.new
          end
          delayed_outstrings.each do |i_item|
            delayed_instruction.attributes["b_output_string"].push i_item.text
          end
        end
        delayed_outstrings.clear
      end


      # Now that we have all of the associated labels - build instruction
      if delayed_instruction != nil
        j_block_builder.addCall delayed_instruction.build(j_block_builder_factory.newAbstractCallBuilder(delayed_instruction.name), labels.merge(@labels), l_stack)
        delayed_instruction = nil
      end

      if item.is_a? InstructionBlock
        j_block_builder.addBlock(item.build j_block_builder_factory, labels.merge(@labels), l_stack)
        delayed_instruction = nil
      elsif item.is_a? Instruction
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