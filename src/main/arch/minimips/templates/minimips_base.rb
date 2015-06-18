#
# Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require ENV['TEMPLATE']

class MiniMipsBaseTemplate < Template

  def initialize
    super
    # Initialize settings here 

    # Sets the indentation token used in test programs
    @indent_token = "\t"

    # Sets the token used in separator lines printed into test programs
    @separator_token = "="
  end

  def pre
    data_config(:text => '.data', :target => 'M', :addressableSize => 8) {
      define_type :id => :byte, :text => '.byte', :type => type('card', 8)
      define_type :id => :half, :text => '.half', :type => type('card', 16)
      define_type :id => :word, :text => '.word', :type => type('card', 32)

      define_space :id => :space, :text => '.space', :fillWith => 0
      define_ascii_string :id => :ascii, :text => '.ascii', :zeroTerm => false
      define_ascii_string :id => :asciiz, :text => '.asciiz', :zeroTerm => true
    }

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified register (target) via the REG addressing mode.
    #
    # This is the default preparator. It is used when no special case
    # previded below is applicable.
    #
    preparator(:target => 'REG') {
      lui  target, value(16, 31)
      addi target, target, value(0, 15)
    }

    #
    # Special case: Target is $zero register. Since it is read only and
    # always equal zero, it makes no sence to initialize it.
    #
    preparator(:target => 'REG', :arguments => {:i => 0}) {
      # Empty
    }

    #
    # Special case:  Value equals 0x00000000. In the case, it is
    # more convenient to use $zero register to reset the target.
    #
    preparator(:target => 'REG', :mask => "00000000") {
      OR target, zero, zero
    }
  end

  def post
    # Place your finalization code here
  end

  # Alias for the NOP instruction (MIPS idiom)
  def nop
    sll zero, zero, 0
  end

  # Aliases for accessing General-Purpose Registers
  #   Name    Number Usage                Preserved?
  #   $zero      0   Constant zero
  #   $at        1   Reserved (assembler)
  #   $v0–$v1   2–3  Function result
  #   $a0–$a3   4–7  Function arguments
  #   $t0–$t7  8–15  Temporaries
  #   $s0–$s7  16–23 Saved                    yes
  #   $t8–$t9  24–25 Temporaries
  #   $k0–$k1  26-27 Reserved (OS)
  #   $gp       28   Global pointer           yes
  #   $sp       29   Stack pointer            yes
  #   $fp       30   Frame pointer            yes
  #   $ra       31   Return address           yes

  def zero
    reg(0)
  end

  def at
    reg(1)
  end

  def v0
    reg(2)
  end

  def v1
    reg(3)
  end

  def a0
    reg(4)
  end

  def a1
    reg(5)
  end

  def a2
    reg(6)
  end

  def a3
    reg(7)
  end

  def t0
    reg(8)
  end

  def t1
    reg(9)
  end

  def t2
    reg(10)
  end

  def t3
    reg(11)
  end

  def t4
    reg(12)
  end

  def t5
    reg(13)
  end

  def t6
    reg(14)
  end

  def t7
    reg(15)
  end

  def s0
    reg(16)
  end

  def s1
    reg(17)
  end

  def s2
    reg(18)
  end

  def s3
    reg(19)
  end

  def s4
    reg(20)
  end

  def s5
    reg(21)
  end

  def s6
    reg(22)
  end

  def s7
    reg(23)
  end

  def t8 
    reg(24)
  end

  def t9
    reg(25)
  end

  def k0 
    reg(26)
  end

  def k1 
    reg(27)
  end

  def gp
    reg(28)
  end

  def sp
    reg(29)
  end

  def fp
    reg(30)
  end

  def ra
    reg(31)
  end

  #
  # Shortcut methods to access memory resources in debug messages
  #

  def gpr_observer(index)
    location('GPR', index)
  end

  def mem_observer(index)
    location('M', index)
  end

  #
  # Utility method for printing data stored in memory using labels.
  #
  def trace_data(begin_label, end_label)
    begin_addr = address(begin_label)
    end_addr = address(end_label)

    count = (end_addr - begin_addr) / 4

    trace "\nData starts: %d", begin_addr
    trace "Data ends:   %d", end_addr
    trace "Data count:  %d", count

    trace "\nData values:"
    (0..(count-1)).each { |i| trace "M[%d]: %d", i, mem_observer(i) }
    trace ""
  end
end
