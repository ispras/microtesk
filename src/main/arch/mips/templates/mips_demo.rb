# Simple demo template for MIPS

require ENV['TEMPLATE']

class MipsDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    comment "MIPS TEST"

    # "Plain" sequence
    add 3, REG_IND_ZERO(1), REG_IND_ZERO(2)
    sub 3, REG_IND_ZERO(1), REG_IND_ZERO(2)

    addi 3, REG_IND_ZERO(0), IMM16(0x1)
    addi 3, REG_IND_ZERO(3), IMM16(0x1)
    
    # Randomized sequence
    block(:compositor => "RANDOM", :combinator => "RANDOM") {
      add 3, REG_IND_ZERO(1), REG_IND_ZERO(2)
      sub 3, REG_IND_ZERO(1), REG_IND_ZERO(2)
      
      addi 3, REG_IND_ZERO(0), IMM16(0x1)
      addi 3, REG_IND_ZERO(3), IMM16(0x1)
    }

    # prints register state after initialization
    print_all_registers
    trace "This is a debug message"

    # block { }

  end

  def print_all_registers
    trace "\nDEBUG: GRP values:"
    (0..31).each { |i| trace "GRP[%d] = %s", i, location("GPR", i) }
    trace ""
  end

end
