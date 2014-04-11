# Simple demo template for MIPS

require ENV['MTRUBY']

class MipsDemo < Template

  def initialize
    super
    @is_executable = yes
  end

  def run
    puts "# MIPS TEST"

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
    # print_all_registers

    debug { puts "This is a debug message" }

    # block { }

  end

  def print_all_registers

    debug {
      a = "DEBUG: GRP values: "
      (0..31).each do |i|
         s = sprintf("%034b", get_loc_value("GPR", i))
         a += s[2, s.length] + ", "
      end
      puts a
    }

  end

end
