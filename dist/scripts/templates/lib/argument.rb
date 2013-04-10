
# This class describes a single argument defined by an addressing mode and its values

# Conventions: integers are translated into an argument with an "#IMM" mode. Nil means the simulator is responsible for
# setting that argument

class Argument

  attr_accessor :mode, :values, :definition#, :attributes

  # @values: String -> String or integer

  def initialize
    @mode = "untitled"
    @values = Hash.new
#    @definition = ArgumentDefinition.new
  end

  def j_build(j_arg_builder)
    #if @definition.modes.include?(@mode)

    j_builder = j_arg_builder.getModeBuilder(@mode)

    @values.each do |k, v|
      j_builder.setArgumentValue(k.to_s, v)
    end

    #elsif
    #  raise "MTRuby Argument Exception: unknown addressing mode '" + @mode + "'"
    #end
  end

end