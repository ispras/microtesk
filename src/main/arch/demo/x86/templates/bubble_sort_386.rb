#
# Copyright 2017 ISP RAS (http://www.ispras.ru)
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

require_relative 'x86_base'

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described test program is a simple implemention of
# the bubble sort algorithm. The algorithm in pseudocode (from Wikipedia):
#
# procedure bubbleSort( A : list of sortable items )
#   n = length(A)
#   repeat
#     swapped = false
#     for i = 1 to n-1 inclusive do
#       /* if this pair is out of order */
#       if A[i-1] > A[i] then
#         /* swap them and remember something changed */
#         swap( A[i-1], A[i] )
#         swapped = true
#       end if
#     end for
#   until not swapped
# end procedure
#

# The test program can be executed in the https://www.tutorialspoint.com/compile_assembly_online.php
# Use: nasm -f elf *.asm; ld -m elf_i386 -s -o demo *.o -Tdata 0x1000
class BubbleSortTemplatei386 < X86BaseTemplate
  def pre
    super

    data {
      label :data
      word rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)
      label :end
    }
  end
  def run
    sequence {
      text "cpu 8086"
      newline

      mov_r16i16 bx, IMM16(0x1000)
      mov_m16i16 ds, RIAM_BX(), IMM16(7)
      add_r16i16 bx, IMM16(2)
      mov_m16i16 ds, RIAM_BX(), IMM16(3)
      add_r16i16 bx, IMM16(2)
      mov_m16i16 ds, RIAM_BX(), IMM16(3)
      add_r16i16 bx, IMM16(2)
      mov_m16i16 ds, RIAM_BX(), IMM16(9)
      add_r16i16 bx, IMM16(2)
      mov_m16i16 ds, RIAM_BX(), IMM16(1)

      mov_r16i16 dx, IMM16(8)
      trace "bx = %x", gpr_observer(3)

      ########################### Outer loop starts ##############################
      label :repeat

      mov_r16i16 ax, IMM16(2)
      ########################### Inner loop starts ##############################
      label :for
      cmp_r16r16 ax, dx
      trace "ax = %x", gpr_observer(0)
      trace "dx = %x", gpr_observer(2)
      je :exit_for

      mov_r16r16 bx, ax
      add_r16i16 bx, IMM16(0x1000)
      mov_r16m16 ds, cx, RIAM_BX()
      sub_r16i16 bx, IMM16(0x2)
      cmp_r16m16 ds, cx, RIAM_BX()

      jle :next
      mov_r16m16 ds, dx, RIAM_BX()
      mov_m16r16 ds, RIAM_BX(), cx
      add_r16i16 bx, IMM16(0x2)
      mov_m16r16 ds, RIAM_BX(), dx
      mov_r16i16 bx, IMM16(0x1020)
      mov_m16i16 ds, RIAM_BX(), IMM16(0x1)

      label :next
      add_r16i16 ax, IMM16(2)
      mov_r16i16 dx, IMM16(8)

      jmp_long :for
      ############################ Inner loop ends ###############################
      label :exit_for

      mov_r16i16 bx, IMM16(0x1020)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)

      cmp_m16i16 ds, RIAM_BX(), IMM16(0x0)
      mov_m16i16 ds, RIAM_BX(), IMM16(0x0)
      jne :repeat
      ############################ Outer loop ends ###############################

      #
      mov_r16i16 bx, IMM16(0x1000)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)
      add_r16i16 bx, IMM16(2)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)
      add_r16i16 bx, IMM16(2)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)
      add_r16i16 bx, IMM16(2)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)
      add_r16i16 bx, IMM16(2)
      mov_r16m16 ds, ax, RIAM_BX()
      trace "ax = %x", gpr_observer(0)
    }.run
  end

end
