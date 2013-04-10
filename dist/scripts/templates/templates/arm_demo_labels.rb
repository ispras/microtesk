# Demo template time!

require_relative "../mtruby"
require_relative "./demo_prepost"

class ArmDemo < DemoPrepost
  def initialize
    super
    @is_executable = yes
  end

  def run

    sub blank, setsoff, reg(0), reg(0), register0

    label :valiant

    ADD_IMMEDIATE blank, setsOff, REG(0), REG(0), IMMEDIATE(0, 2)
    cmp_immediate blank, reg(0), immediate(0, 5)

    # Uncomment here to list all GPR registers
    exec_debug {
        a = ""
        (0..15).each do |i|
          a += get_reg_value("GPR", i).to_s + ", "
        end
        puts a
    }

    b notEqual, :valiant

    sub blank, setsoff, reg(1), reg(1), register1
    add_immediate blank, setsoff, reg(1), reg(1), immediate(0, 1)

    #b notequal, :defiant
    b blank, :defiant

    #add_immediate blank, setsoff, reg(2), reg(2), immediate(0, 2)
    sub blank, setsoff, reg(0), reg(0), register0

    label :defiant

    sub blank, setsoff, reg(1), reg(1), register1
    #add_immediate blank, setsoff, reg(3), reg(3), immediate(0, 3)

  end
end