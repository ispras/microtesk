class Instruction

  attr_accessor :attributes, :arguments, :instruction_name, :situation, :block_id

  # Mostly instance variables
  def initialize

    @instruction_name = "UntitledInstruction"

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

  # Instruction construction
  def build(j_instruction_builder, labels)

    @attributes["labels"] = Array.new
    @labels.each do |label|
      @attributes["labels"].push [label, labels[label]]
    end

    @attributes.each_pair do |key, value|
      j_instruction_builder.setAttribute(key, value)
    end

    @arguments.each_pair do |name, value|
      if value.is_a? String or value.is_a? Object
        j_instruction_builder.setArgumentImmediate(name, 0)
      elsif value.is_a? Integer
        j_instruction_builder.setArgumentImmediate(name, value)
      elsif value.is_a? Argument
        value.build j_instruction_builder.setArgumentUsingBuilder(name, value.name)
      end
    end

    # Logic?..
    s_b = j_instruction_builder.setTestSituation(@situation)
    s_b.setSituation(s_b.build)

    j_instruction_builder.build()

  end


end