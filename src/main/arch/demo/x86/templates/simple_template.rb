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
    MOV_R16IMM16 ax, IMM16(11)
    MOV_R16R16 cx, ax
    trace "ax = %x", gpr_observer(0)
    trace "cx = %x", gpr_observer(1)
    MOV_R16IMM16 dx, IMM16(2)
    trace "dx = %x", gpr_observer(2)
    MUL_R16 ax
    trace "ax = %x", gpr_observer(0)
    trace "dx = %x", gpr_observer(2)
    MOV_R16IMM16 bx, IMM16(7)
    MOV_R16IMM16 cx, IMM16(3)
    ADD_R16R16 bx, cx
    trace "cx = %x", gpr_observer(1)
    trace "bx = %x", gpr_observer(3)
    MOV_R16IMM16 ax, IMM16(2)
    MOV_R16IMM16 cx, IMM16(5)
    SUB_R16R16 ax, cx
    trace "ax = %x", gpr_observer(0)
    trace "cx = %x", gpr_observer(1)
    AND_R16R16 ax, cx
    OR_R16R16 dx, bx
    OR_R16IMM16 ax, IMM16(0xbb)
    AND_R16IMM16 bx, IMM16(0xcc)
    ADD_R16IMM16 cx, IMM16(0xdd)
    SUB_R16IMM16 dx, IMM16(0xee)
  end

end
