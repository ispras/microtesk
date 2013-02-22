#                              #
#   MicroTESK Ruby front-end   #
#                              #
#      Data storage class      #
#                              #

class StorageRange
attr_accessor :name, :begin, :end

  def initialize
    @name = "untitled"
    @begin = 0
    @end = 0
  end

end