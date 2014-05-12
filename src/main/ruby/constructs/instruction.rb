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