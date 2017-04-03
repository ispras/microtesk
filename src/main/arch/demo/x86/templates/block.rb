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
    # Adds nop to all test cases as a placeholder to return from an exception
    epilogue { }

    # Produces a single test case that consists of three instructions
    sequence {
      MOV_R16R16 ax, bx
      SUB_R16R16 cx, dx
      ADD_R16R16 gpr16(_), gpr16(_)
    }.run

    # Atomic sequence. Works as sequence in this context.
    atomic {
      MOV_R16R16 ax, bx
      ADD_R16R16 cx, dx
      SUB_R16R16 gpr16(_), gpr16(_)
    }.run

    # Produces three test cases each consisting of one instruction
    iterate {
      MOV_R16R16 ax, bx
      SUB_R16R16 cx, dx
      ADD_R16R16 gpr16(_), gpr16(_)
    }.run

    # Produces four test cases consisting of two instructions
    # (Cartesian product composed in a random order)
    block(:combinator => 'product', :compositor => 'random') {
      iterate {
        SUB_R16R16 cx, dx
        ADD_R16R16 ax, bx
      }

      iterate {
        SUB_R16R16 gpr16(_), gpr16(_)
      }
    }.run

    # Merges two sequnces in random fashion. Atomic sequences are unmodifiable.
    block(:combinator => 'diagonal', :compositor => 'random', :obfuscator => 'random') {
      sequence {
        SUB_R16R16 bx, ax
        OR_R16R16 cx, dx
      }

      atomic {
        prologue { comment 'Atomic starts' }
        epilogue { comment 'Atomic ends' }

        AND_R16R16 gpr16(_), gpr16(_)
      }
    }.run
  end

end
