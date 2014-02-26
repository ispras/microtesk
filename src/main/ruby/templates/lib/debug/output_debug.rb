# Code that runs during file output
# Whatever is returned from the @proc is written to the file, so be careful

class OutputDebug

  attr_accessor :proc

  def initialize

    @proc = lambda do
      "// Debug"
    end

  end

end
