# Demo template time!

require ENV["TEMPLATE"]

require_relative "./demo_prepost"

class ArmDemo < DemoPrepost

  def initialize
    super
    @is_executable = true
  end

  def run
    # prints register state after initialization
    print_all_registers

    # add_immediate blank, setsoff, reg(0), reg(0), immediate(0, 1)
    # add_immediate blank, setsoff, reg(0), reg(0), immediate(4, 1)
    # add_immediate blank, setsoff, reg(0), reg(0), immediate(8, 1)
    
    add equalcond, setsoff, reg(2), reg(2), register2

    print_all_registers

    add equalcond, setson, reg({:r => 0}), reg({:r => 0}), register1
    add equalcond, setsoff, reg(1), reg(2), register3 do overflow end

    debug { puts "This is a debug message" }

    add equalcond, setsoff, reg(2), reg(2), register0 do overflow end

    add equalcond, setsoff, reg(3), reg(2), register0 do random end

    add equalcond, setsoff, reg(4), reg(2), register0 do normal end
    add equalcond, setsoff, reg(5), reg(2), register0 do zero end

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

    debug {
      a = "DEBUG: GRP values: "
      (0..15).each do |i|
         s = sprintf("%034b", get_loc_value("GPR", i))
         a += s[2, s.length] + ", "
      end
      puts a
    }

  end

end
