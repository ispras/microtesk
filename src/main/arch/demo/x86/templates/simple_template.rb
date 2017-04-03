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
    trace "r8 = %x", gpr_observer(8)
    MOV_R16IMM16 r8w, 11
    MOV_R16R16 r9w, r8w
    trace "r8 = %x", gpr_observer(8)
    trace "r9 = %x", gpr_observer(9)
    MOV_R16IMM16 r9w, 2
    trace "r9 = %x", gpr_observer(9)
    MUL_R16 r10w
    trace "r10 = %x", gpr_observer(10)
    trace "dx = %x", gpr_observer(2)
    MOV_R16IMM16 r11w, 7
    MOV_R16IMM16 r12w, 3
    ADD_R16R16 r11w, r12w
    trace "r11 = %x", gpr_observer(11)
    trace "r12 = %x", gpr_observer(12)
    MOV_R16IMM16 r13w, 2
    MOV_R16IMM16 r14w, 5
    SUB_R16R16 r13w, r14w
    trace "r13 = %x", gpr_observer(13)
    trace "r14 = %x", gpr_observer(14)
    AND_R16R16 r11w, r12w
    OR_R16R16 r13w, r14w
    OR_R16IMM16 r15w, 0xbb
    AND_R16IMM16 r15w, 0xcc
    ADD_R16IMM16 r15w, 0xdd
    SUB_R16IMM16 r15w, 0xee
  end

end
