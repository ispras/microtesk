# Simple demo template for MIPS

require ENV['MTRUBY']

class MipsDemo < Template

  def initialize
    super
    @is_executable = yes
  end

  def run
    add r(3), r(1), r(2)
    add r(3), r(1), r(2)

  end

end
