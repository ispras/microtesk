#                              #
#   MicroTESK Ruby front-end   #
#                              #
#  Situation description class #
#                              #

require_relative "situation"

class SituationDefinition
  attr_accessor :name, :arguments#, :attributes

  def initialize
    @name = "untitled"
    @arguments = Hash.new
  end

  def getSituation (arguments={})
    temp = Situation.new
    temp.name = @name
    temp.arguments = arguments

    arguments.each_key do |key|
      if !@arguments.has_key?(key.to_s)
        puts "MTRuby: warning: Situation '" + @name + "' has no argument called '" +
             key.to_s + "'"
      end
    end

    temp
  end

end