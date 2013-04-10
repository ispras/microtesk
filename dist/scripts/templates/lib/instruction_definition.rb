
# This class represents a definition of an instruction - might end up being useless.

class InstructionDefinition
  attr_accessor :name, :argument_defs

  # argument_defs: Array of ArgumentDefinition

  def initialize
    @name = "untitled"
    @argument_defs = Array.new
  end



end