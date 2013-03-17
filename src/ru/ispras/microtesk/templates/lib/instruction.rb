
# This class represents an instruction in a block, ready to be executed and to receive its own assembler representation

class Instruction

  attr_accessor :name, :arguments, :code, :definition, :situations#, :attributes

  # arguments: Array of Argument

  def initialize
    @name = "untitled"
    @arguments = Hash.new
    #@situations = Array.new
    @code = ""
    #@definition = InstructionDefinition.new
    #@possible_labels = Array.new # of Integer
  end

  def outlog
    puts @code
  end

  def output(file)
    file.puts @code
  end

  def j_build(j_call_builder, template)

    @arguments.each do |s, a|
      if(a.is_a?(Symbol))
        a1 = Argument.new
        a1.mode = "#IMM"
        a1.values[template.registered_modes["#IMM"].first] = template.send(a)
        a1.j_build(j_call_builder.getArgumentBuilder(s))
      else
        a.j_build(j_call_builder.getArgumentBuilder(s))
      end
    end

    # TODO > j_build situations

  end

end