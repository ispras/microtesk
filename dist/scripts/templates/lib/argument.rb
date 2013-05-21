class Argument

  attr_accessor :name, :values

  def initialize

    @name = "UntitledArgument"

    # String -> Object
    @values = Hash.new

  end

  def build(j_arg_builder)
    @values.each_pair do |key, value|
      j_arg_builder.setArgument(key, value)
    end

    j_arg_builder.build()
  end

end