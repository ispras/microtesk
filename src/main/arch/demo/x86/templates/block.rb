#
# MicroTESK X86 Edition
#
# Copyright (c) 2017 Institute for System Programming of the Russian Academy of Sciences
# All Rights Reserved
#
# Institute for System Programming of the Russian Academy of Sciences (ISP RAS)
# 25 Alexander Solzhenitsyn st., Moscow, 109004, Russia
# http://www.ispras.ru
# 

require_relative 'x86_base'

#
# Description:
#
# This test template demonstrates how to use instruction blocks.
#
class BlockTemplate < X86BaseTemplate

  def run
    # Produces a single test case that consists of three instructions
    sequence {
      mov_r16r16 ax, bx
      sub_r16r16 cx, dx
      add_r16r16 gpr16(_), gpr16(_)
    }.run

    # Atomic sequence. Works as sequence in this context.
    atomic {
      mov_r16r16 ax, bx
      add_r16r16 cx, dx
      sub_r16r16 gpr16(_), gpr16(_)
    }.run

    # Produces three test cases each consisting of one instruction
    iterate {
      mov_r16r16 ax, bx
      sub_r16r16 cx, dx
      add_r16r16 gpr16(_), gpr16(_)
    }.run

    # Produces four test cases consisting of two instructions
    # (Cartesian product composed in a random order)
    block(:combinator => 'product', :compositor => 'random') {
      iterate {
        sub_r16r16 cx, dx
        add_r16r16 ax, bx
      }

      iterate {
        mov_r16r16 ax, bx
        sub_r16r16 gpr16(_), gpr16(_)
      }
    }.run

    # Merges two sequnces in random fashion. Atomic sequences are unmodifiable.
    block(:combinator => 'diagonal', :compositor => 'random', :obfuscator => 'random') {
      sequence {
        sub_r16r16 bx, ax
        or_r16r16 cx, dx
      }

      atomic {
        prologue { comment 'Atomic starts' }
        epilogue { comment 'Atomic ends' }

        and_r16r16 gpr16(_), gpr16(_)
      }
    }.run
  end

end
