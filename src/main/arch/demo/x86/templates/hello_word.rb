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

class HelloWordTemplate < X86BaseTemplate
  def run
    trace "ax = %x", gpr_observer(0)
    text "mov	edx,len"
    text "mov	ecx,msg"
    MOV_R32IMM32 ebx, 1
    MOV_R32IMM32 eax, 4
    text "int	0x80"

    MOV_R32IMM32 eax, 1
    text "int	0x80"

    text "section	.data"
    text "msg	db	'Hello, world!',0xa"
    text "len	equ	$ - msg"
  end

end
