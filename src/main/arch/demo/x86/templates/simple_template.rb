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
    mov_r16i16 ax, IMM16(11)
    mov_r16r16 ax, ax
    trace "ax = %x", gpr_observer(0)
    trace "cx = %x", gpr_observer(1)
    mov_r16i16 dx, IMM16(2)
    trace "dx = %x", gpr_observer(2)
    mul_r16 ax
    trace "ax = %x", gpr_observer(0)
    trace "dx = %x", gpr_observer(2)
    mov_r16i16 bx, IMM16(7)
    mov_r16i16 cx, IMM16(3)
    add_r16r16 bx, cx
    trace "cx = %x", gpr_observer(1)
    trace "bx = %x", gpr_observer(3)
    mov_r16i16 ax, IMM16(2)
    mov_r16i16 cx, IMM16(5)
    sub_r16r16 ax, cx
    trace "ax = %x", gpr_observer(0)
    trace "cx = %x", gpr_observer(1)
    and_r16r16 ax, cx
    or_r16r16 dx, bx
    or_r16i16 ax, IMM16(0xbb)
    and_r16i16 bx, IMM16(0xcc)
    add_r16i16 cx, IMM16(0xdd)
    sub_r16i16 dx, IMM16(0xee)

    mov_r16i16 cx, IMM16(0)
    trace "cx = %x", gpr_observer(1)

    mov_r16i16 ax, IMM16(200)
    mov_r16i16 bx, IMM16(8)
    mov_rsegr16 ds, ax
    mov_m16r16 ds, RIAM_BX(), ax
    mov_r16m16 ds, cx, RIAM_BX()
    trace "ax = %x", gpr_observer(0)
    trace "bx = %x", gpr_observer(3)
    trace "cx = %x", gpr_observer(1)
  end

end
