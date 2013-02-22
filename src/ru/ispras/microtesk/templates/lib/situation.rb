#                              #
#   MicroTESK Ruby front-end   #
#                              #
#       Situation class        #
#                              #

class Situation
  attr_accessor :name, :arguments#, :attributes

  def initialize
    @name = "untitled"
    @arguments = Hash.new
  end



end