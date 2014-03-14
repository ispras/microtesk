# Simple demo template for MIPS

require_relative "../../../libs/ruby/mtruby"

class MipsDemo < Template

  def initialize
    super
    @is_executable = yes
  end

  def run
#    prints register state after initialization
#    print_all_registers
    
    debug { puts "This is a debug message" }

#    block {
#    }

  end

#  def print_all_registers
#
#    debug {
#      a = "DEBUG: GRP values: "
#      (0..15).each do |i|
#         s = sprintf("%034b", get_loc_value("GPR", i))
#         a += s[2, s.length] + ", "
#      end
#      puts a }
#
#  end

end
