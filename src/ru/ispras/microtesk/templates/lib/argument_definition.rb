
# This class defines the kind of argument that an instruction expects

class ArgumentDefinition

  attr_accessor :modes, :name

  # @modes: array of String

  def initialize
     @name = "untitled"
     @modes = Array.new
  end

end