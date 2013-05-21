# Code that runs during test sequence execution

class RuntimeDebug

  attr_accessor :proc

  def initialize

    @proc = lambda do
      puts "Debug"
    end

  end

end