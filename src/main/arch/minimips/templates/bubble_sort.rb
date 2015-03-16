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
class BubbleSortTemplate < MiniMipsBaseTemplate
  def pre
    super

    data {
      label :data
      word rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)
      label :end
      space 1
    }
  end

  def run
    text  '.text'
    trace '.text'

    trace_data :data, :end

    la s0, :data
    la s1, :end

    add t0, zero, zero
    ########################### Outer loop starts ##############################
    label :repeat

    addi t1, s0, 4
    ########################### Inner loop starts ##############################
    label :for
    beq t1, s1, :exit_for

    addi t3, zero, 4
    sub  t2, t1, t3

    lw t4, 0, t1
    lw t5, 0, t2

    slt t6, t4, t5
    beq t6, zero, :next
    nop

    swap t4, t5
    addi t0, zero, 1

    sw t4, 0, t1
    sw t5, 0, t2

    label :next
    j :for
    addi t1, t1, 4
    ############################ Inner loop ends ###############################
    label :exit_for

    bne t0, zero, :repeat
    add t0, zero, zero
    ############################ Outer loop ends ###############################

    trace_data :data, :end
  end

  def swap(reg1, reg2)
    xor reg1, reg1, reg2
    xor reg2, reg1, reg2
    xor reg1, reg1, reg2
  end

end
