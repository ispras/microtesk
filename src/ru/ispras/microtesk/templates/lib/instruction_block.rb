
# This class represents a block of instructions with attached test situation. This is the base abstraction that the
# templates work with.

class InstructionBlock
  attr_accessor :instructions, :situations, :j_caller

  # @instructions: Array of Instruction
  # TODO > @situations: Array of Situation

  def initialize
    @instructions = Array.new
    @situations = Array.new
    @code = Array.new
    @j_caller = nil
  end

  def outlog
    #@instructions.each {|i| i.output }
    @code.each {|c| puts c}
  end

  def output(file)
    #@instructions.each {|i| i.output(file)}
    @code.each {|c| file.puts c}
  end

  def j_build(j_simulator)
    j_block_builder = j_simulator.createCallBlock()
    @instructions.each do |i|
      i.j_build(j_block_builder.addCall(i.name))
    end

    # TODO > situations

    @j_caller = j_block_builder.getCallBlock()

  end

  def j_call
    if j_caller == nil
      puts "MTRuby: Trying to call an uninitialized instruction block"
      return
    end

    @code = Array.new

    (0 .. j_caller.getCount() - 1).each do |i|
      j_call = j_caller.getCall(i)
      j_call.execute()
      @code.push(j_call.getText())
    end

  end

  def receive(instruction)
    @instructions.push(instruction)
  end

end