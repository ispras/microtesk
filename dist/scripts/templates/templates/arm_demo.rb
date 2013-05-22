# Demo template time!

require_relative "../mtruby"
require_relative "./demo_prepost"

class ArmDemo < DemoPrepost
  def initialize
    super
    @is_executable = yes
  end

  def run

    add equalcond, setson, reg({:r => 0}), reg({:r => 0}), register1

    add equalcond, setsoff, reg(1), reg(2), register0 do random end

    debug do
      puts "This is a debug message"
    end
    
    add equalcond, setsoff, reg(2), reg(2), register0
    add equalcond, setsoff, reg(3), reg(2), register0

    add equalcond, setsoff, reg(4), reg(2), register0
    add equalcond, setsoff, reg(5), reg(2), register0

    block {
      add equalcond, setsoff, reg(1), reg(3), register0
      add equalcond, setsoff, reg(2), reg(3), register0
      add equalcond, setsoff, reg(3), reg(3), register0
      add equalcond, setsoff, reg(4), reg(3), register0
      add equalcond, setsoff, reg(5), reg(3), register0
    }

    add_immediate blank, setsoff, reg(2), reg(3), immediate(4, 5) do random end
    
    b equalcond, 42 do random end

  end
end
