#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

class  BubbleSortTemplate < MiniMipsBaseTemplate


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


  #t0 array address
  addi at, zero, :length
  lw a0, 0, at

  la a1, :array

  add t0, zero, zero #counter of the top-level loop

  addi at, zero, 1
  sub t7, a0, at

  label :loop
    trace "%x", gpr(8)
    trace "%x", gpr(4)
    beq t0, a0, :exit
    sll zero, zero, 0 #DELAY SLOT

    add t1, zero, zero


    label :loop1
       beq t7, t1, :exit1
       sll zero, zero, 0 #DELAY SLOT

       addi at, zero, 4
       mult t1, at
       mflo t3

       add t3, t3, a1
       addi t4, t3, 4
       
       lw t2, 0, t3
       lw t5, 0, t4
       
       slt at, t2, t5
       bne at, zero, :cont
       sll zero, zero, 0 #DELAY SLOT
       
       #SWAP
       add t2, t2, t5
       sub t5, t2, t5
       sub t2, t2, t5
       
       sw t2, 0, t3
       sw t5, 0, t4
       
       label :cont
       addi t1, t1, 1
       
       j :loop1
       sll zero, zero, 0 #DELAY SLOT

    label :exit1
    
    addi t0, t0, 1
    j :loop
    sll zero, zero, 0 #DELAY SLOT
  label :exit


#OUTPUT
    
    la a0, :array
    i = 0
    while i < 12 do
    	lw v1, 0, a0    
        #trace "%x", gpr(5)
        trace "%x", gpr(3)
        addi a0, a0, 4
        
        i +=1
    end
    end
end