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

class SimpleTemplate < X86BaseTemplate
  def run
    trace "ax = %x", gpr_observer(0)
    MOV_R16IMM16 ax, 11
    MOV_R16R16 bx, ax
    trace "ax = %x", gpr_observer(0)
    trace "bx = %x", gpr_observer(3)
    MOV_R16IMM16 bx, 2
    trace "bx = %x", gpr_observer(3)
    MUL_R16 bx
    trace "ax = %x", gpr_observer(0)
    trace "dx = %x", gpr_observer(2)
    MOV_R16IMM16 ax, 7
    MOV_R16IMM16 bx, 3
    ADD_R16R16 ax, bx
    trace "ax = %x", gpr_observer(0)
    trace "bx = %x", gpr_observer(3)
    MOV_R16IMM16 ax, 2
    MOV_R16IMM16 bx, 5
    SUB_R16R16 ax, bx
    trace "ax = %x", gpr_observer(0)
    trace "bx = %x", gpr_observer(3)
    AND_R16R16 ax, bx
    OR_R16R16 ax, bx
  end

end
