#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Data storage          #
#      description class       #
#                              #

require_relative "storage"
require_relative "storage_range"

class StorageDefinition
attr_accessor :name, :capacity

  def initialize
    @capacity = 0
    @name = "untitled"
  end

  def [] (index)
    if index.class <= Integer
      temp = Storage.new
      temp.name = name
      temp.index = index
      temp
    elsif index.class <= Range
      temp = StorageRange.new
      temp.name = name
      temp.begin = index.begin
      temp.end = index.end
      temp
    else
      raise "MTRuby: error: " + caller[0] + ": " + @name +
            "Index of an incompatible type"
    end
  end

end