# Simple demo template for MIPS

require ENV["TEMPLATE"]

class MipsDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    add r(3), r(1), r(2)
    add r(3), r(1), r(2)

  end

end
