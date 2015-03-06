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

# Description
# This is a test template with a program of insertion 
# sorting algorithm written for mini-MIPS processor.
# It can be applied to the mini-MIPS model to
# simulate its behaviour.
#
# Algorithm:
# for i = 1 ... n:
#     for j = (i - 1) ... 1:
#         if (A[i] < A[j]): 
#             if (j == 1):
#                 copy = A[i]
#                 for k = i ... 1:
#                     A[k] = A[k - 1]
#                 A[1] = copy 
#         else:
#             copy = A[i]
#             for k = i ... (j + 1):
#                 A[k] = A[k - 1]
#             A[j] = copy 


class InsertionSortTemplate < MiniMipsBaseTemplate

  def pre
    super

    data {
      label :array
      word rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)
      label :end
      space 1
    }
  end

  def run
    print_data

    la s0, :end
    la s1, :array
    addi s2, zero, 1
    
    addi t0, s1, 1 #counter of the top-level loop
    ########################### Array iteration loop starts #################
    label :loop
    beq t0, s0, :exit
    nop

    lw t1, 0, t0 #load new element to insert

    addi t8, zero, 1
    sub t3, t0, t8 #counter for searching loop

    add t4, zero, t0 #counter for the shift loop
    ########################### Place search loop starts ####################
    label :loop1
        
    lw t2, 0, t3 #loading the value of new compared element

    slt t5, t1, t2
    bne t5, zero, :cont 
    nop

    addi t3, t3, 1 #return index to previously compared element
    ########################### Shift loop starts ###########################
    label :loop2 
    beq t3, t4, :insertion #if the shift is done, jump to inserting and 
                           #next iteration of loop "loop"
    nop

    addi t8, zero, 1
    sub t7, t4, t8
    
    lw t6, 0, t7
    sw t6, 0, t4
    
    addi t8, zero, 1
    sub t4, t4, t8 #decrement of the counter

    j :loop2
    sub t4, t4, s2
    ########################### Shift loop ends ##############################
    label :cont
    add t4, zero, t0 #address of current shifting position #
                     #(used if we do the branch in next instruction)
    beq t3, s1, :loop2
    nop

    j :loop1
    sub t3, t3, s2
    ########################### Place search loop ends ######################
    label :insertion

    sw t1, 0, t3 #insertion
    
    j :loop
    addi t0, t0, 1 #increment for the counter
    ########################### Array iteration loop  ends ##################
    label :exit
    
    print_data
  end

  def print_data
    count = (address(:end) - address(:array)) / 4

    trace "\nData starts: %d", address(:array)
    trace "Data ends:   %d", address(:end)
    trace "Data count:  %d", count

    trace "\nData values:"
    (0..(count-1)).each { |i| trace "M[%d]: %d", i, mem(i) }
    trace ""
  end

end
