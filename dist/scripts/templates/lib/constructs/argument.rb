class Argument

  attr_accessor :name, :values, :mode

  def initialize

    #@name = "UntitledArgument"
    @mode = "UntitledMode"

    # String -> Object
    @values = Hash.new

  end

  def build(j_arg_builder)
    @values.each_pair do |key, value|
      if value.is_a? NoValue
        # TODO: Handle NoValue
      else
        j_arg_builder.setArgument(key, value)
      end
    end

    j_arg_builder.build()
  end

end