require_relative 'minimips_base'

class  AddingTemplate < MinimipsBaseTemplate

    def run
        addi reg(1), reg(0), 1
        addi reg(2), reg(0), 2
        add reg(3), reg(2), reg(1)

        trace "\nValue in register $3 = %d\n", gpr(3)
    end
end