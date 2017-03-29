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
    trace "ax = %x", gpr_observer(0)
    MOV_R16IMM16 bx, 2
    trace "bx = %x", gpr_observer(3)
    MUL_R16 bx
    trace "ax = %x", gpr_observer(0)
    trace "dx = %x", gpr_observer(2)
  end

end
