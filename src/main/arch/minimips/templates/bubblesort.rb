#
# Copyright 2015 ISP RAS (http://www.ispras.ru)
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

require_relative 'minimips_base'

class BubbleSortTemplate < MiniMipsBaseTemplate
  def pre
    super

    data {
      label :array
      word 1, 6, 2, 7, 5, 6, 3, 8, 9, 5, 6, 3

      label :length
      word 12
    }
  end
  
  def run
    print_data

    la s0, :array
    la s1, :length

    lw s2, 0, s1

    add t0, zero, zero #counter of the top-level loop

    addi at, zero, 1
    sub t7, s2, at

    label :loop
      trace "%x", gpr(8)
      trace "%x", gpr(4)
      beq t0, s2, :exit
      nop

      add t1, zero, zero

      label :loop1
         beq t7, t1, :exit1
         nop

         addi at, zero, 4
         mult t1, at
         mflo t3

         add t3, t3, s0
         addi t4, t3, 4

         lw t2, 0, t3
         lw t5, 0, t4

         slt at, t2, t5
         bne at, zero, :cont
         nop
         
         swap t2, t5

         sw t2, 0, t3
         sw t5, 0, t4

         label :cont
         addi t1, t1, 1

         j :loop1
         nop

      label :exit1

      addi t0, t0, 1
      j :loop
      nop
    label :exit

    print_data
  end

  def swap(reg1, reg2)
    xor reg1, reg1, reg2
    xor reg2, reg1, reg2
    xor reg1, reg1, reg2
  end

  def print_data
    count = (address(:length) - address(:array)) / 4 + 1

    trace "\nData starts: %d", address(:array)
    trace "Data ends:   %d", address(:length)
    trace "Data count:  %d", count

    trace "\nData values:"
    (0..(count-1)).each { |i| trace "M[%d]: %d", i, mem(i) }
    trace ""
  end

end
