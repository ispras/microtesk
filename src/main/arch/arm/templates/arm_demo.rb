# Demo template time!

require ENV['TEMPLATE']

class ArmDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    newline
    
    # prints register state after initialization
    print_all_registers

    add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 1)
    add_immediate blank, setsoff, reg(0), reg(0), immediate(4, 1)
    add_immediate blank, setsoff, reg(0), reg(0), immediate(8, 1)

    add equalcond, setsoff, reg(2), reg(2), register2

    print_all_registers

    add equalcond, setson, reg({:r => 0}), reg({:r => 0}), register1
    add equalcond, setsoff, reg(1), reg(2), register3 do situation('overflow') end

    trace "This is a debug message"

    add equalcond, setsoff, reg(2), reg(2), register0 do situation('overflow') end

    add equalcond, setsoff, reg(3), reg(2), register0 do situation('random') end

    add equalcond, setsoff, reg(4), reg(2), register0 do situation('normal') end
    add equalcond, setsoff, reg(5), reg(2), register0 do situation('zero') end

    block {
      add equalcond, setsoff, reg(1), reg(3), register0
      add equalcond, setsoff, reg(2), reg(3), register0
      add equalcond, setsoff, reg(3), reg(3), register0
      add equalcond, setsoff, reg(4), reg(3), register0
      add equalcond, setsoff, reg(5), reg(3), register0
    }

    add_immediate blank, setsoff, reg(2), reg(3), immediate(4, 5)
    b equalcond, 42

    add equalcond, setsoff, reg(1), reg(3), register10

  end

  def print_all_registers
    trace "\nDEBUG: GRP values:"
    (0..15).each { |i| trace "GRP[%d] = %s", i, location("GPR", i) }
    trace ""
  end

end
