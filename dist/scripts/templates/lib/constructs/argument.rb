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
        #  Handle NoValue
        j_arg_builder.setRandomArgument(key)
      else
        j_arg_builder.setArgument(key, value)
      end
    end

    j_arg_builder.build()
  end

end