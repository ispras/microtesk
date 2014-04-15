class Attribute

  attr_accessor :name, :parameters

  def initialize

    @name = "UntitledAttribute"

    # String -> Object
    @parameters = Hash.new

  end

end