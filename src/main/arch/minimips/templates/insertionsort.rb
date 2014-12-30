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

class  InsertionSortTemplate < MinimipsBaseTemplate


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

    #looks terrible, there sould be "lw" instruction for data
    addi at, zero, :length
    lw a0, 0, at

    la a1, :array
    add s4, zero, a1
    
    addi t0, zero, 1 #counter of the top-level loop

    label :loop
      beq t0, a0, :exit
      sll zero, zero, 0 #DELAY SLOT
    
      addi s2, zero, 4
      mult t0, s2 #computing the address of new element
      mflo a2 #the fact that the result of multiplication is less than
                  #32 bit long is not guaranteed!
      add a2, a2, a1 #computing the address of new element
  
      lw t1, 0, a2 #load new elenent
    
      addi s1, zero, 4 #counter of an inner loop
    
      label :loop1
        sub a3, a2, s1 #computing address of new element in search
       
        lw t2, 0, a3#loading the value of new elem in search

        slt at, t1, t2
        bne at, zero, :cont 
        sll zero, zero, 0 #DELAY SLOT

        addi a3, a3, 4 ##return to the greater number in search
           
       
        add t4, zero, a2 #address of shifting (counter)       
        add t5, zero, t1 #saving the value of element 
                                    #that will be inserted
       
        label :loop2 #shift
          beq a3, t4, :final #the shift is done, jump to 
                                        #inserting and next iteration
          sll zero, zero, 0 #DELAY SLOT
          addi s2, zero, 4
          sub t7, t4, s2
         
          #SHIFT
          lw t6, 0, t7
          sw t6, 0, t4
          #SHIFT
         
          addi s2, zero, 4
          sub t4, t4, s2 #decrement of the counter
          j :loop2
          sll zero, zero, 0 #DELAY SLOT
       
        label :cont
        addi s1, s1, 4 #increment
        j :loop1
        sll zero, zero, 0 #DELAY SLOT
       
      label :final
      sw t5, 0, a3 #inserting
      label :exit1

      addi t0, t0, 1 #decrement for the counter
      j :loop

    sll zero, zero, 0 #DELAY SLOT
    
    label :exit

#OUTPUT
    
    #la a0, array
    #addi a2, zero, 8
    

    #sub a0, a0, a2
    #trace "%x", gpr(4)
    #trace "%x", array
    
    add a0, zero, zero #reset to zero of $4 (temporary hack)
    la a0, :array
    $i = 0
    while $i < 12 do
    	lw v1, 0, a0    
        #trace "%x", gpr(5)
        trace "%x", gpr(3)
        addi a0, a0, 4
        
        $i +=1
    end
    end
end