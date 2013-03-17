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
    add equalcond, setsoff, reg(1), reg(2), register0
    add equalcond, setsoff, reg(1), reg(2), register0
    add equalcond, setsoff, reg(1), reg(2), register0
    add equalcond, setsoff, reg(1), reg(2), register0
    add equalcond, setsoff, reg(1), reg(2), register0

    atomic {
      add equalcond, setsoff, reg(1), reg(2), register0
      add equalcond, setsoff, reg(1), reg(2), register0
      add equalcond, setsoff, reg(1), reg(2), register0
      add equalcond, setsoff, reg(1), reg(2), register0
      add equalcond, setsoff, reg(1), reg(2), register0
    }

    add_immediate negative, setsoff, reg(2), reg(3), immediate({:r => 4, :c => 5})

    b equalcond, 42

  end
end