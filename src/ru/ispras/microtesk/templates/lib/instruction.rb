
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

  def j_build(j_call_builder)

    @arguments.each do |s, a|
      a.j_build(j_call_builder.getArgumentBuilder(s))
    end

    # TODO > j_build situations

  end

end