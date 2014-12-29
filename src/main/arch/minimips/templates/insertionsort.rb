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
        word 1, 6, 2, 7, 5, 6, 3, 8, 9, 5, 6, 7

        label :length
        word 12
      }

    end
    def run

    #looks terrible, there sould be "lw" instruction for data
    addi reg(1), reg(0), :length
    lw reg(4), 0, reg(1)

    la reg(5), :array
    add reg(20), reg(0), reg(5)
    trace "%x", gpr(5)
    
    addi reg(8), reg(0), 1 #counter of the top-level loop

    label :loop
        beq reg(8), reg(4), :exit
        sll reg(0), reg(0), 0 #DELAY SLOT
    
        addi reg(18), reg(0), 4
        mult reg(8), reg(18) #computing the address of new element
        mflo reg(6) #the fact that the result of multiplication is less than
                    #32 bit long is not guaranteed!

        add reg(6), reg(6), reg(5) #computing the address of new element
    
        lw reg(9), 0, reg(6) #load new elenent
    
        addi reg(17), reg(0), 4 #counter of an inner loop
    
        label :loop1
        sub reg(7), reg(6), reg(17) #computing address of new element in search
       
        lw reg(10), 0, reg(7)#loading the value of new elem in search

        slt reg(1), reg(9), reg(10)
        bne reg(1), reg(0), :cont 
        sll reg(0), reg(0), 0 #DELAY SLOT

        addi reg(7), reg(7), 4 ##return to the greater number in search
           
       
        add reg(12), reg(0), reg(6) #address of shifting (counter)       
        add reg(13), reg(0), reg(9) #saving the value of element 
                                    #that will be inserted
       
        label :loop2 #shift
            beq reg(7), reg(12), :final #the shift is done, jump to 
                                        #inserting and next iteration
            sll reg(0), reg(0), 0 #DELAY SLOT
            addi reg(18), reg(0), 4
            sub reg(15), reg(12), reg(18)
         
            #SHIFT
            lw reg(14), 0, reg(15)
            sw reg(14), 0, reg(12)
            #SHIFT
         
            addi reg(18), reg(0), 4
            sub reg(12), reg(12), reg(18) #decrement of the counter
            j :loop2
            sll reg(0), reg(0), 0 #DELAY SLOT
       
        label :cont
        addi reg(17), reg(17), 4 #increment
        j :loop1
        sll reg(0), reg(0), 0 #DELAY SLOT
       
    label :final
    sw reg(13), 0, reg(7)#inserting
    label :exit1
    addi reg(8), reg(8), 1 #decrement for the counter
    j :loop
    sll reg(0), reg(0), 0 #DELAY SLOT
    
    label :exit

#OUTPUT
    
    #la reg(4), array
    #addi reg(6), reg(0), 8
    

    #sub reg(4), reg(4), reg(6)
    #trace "%x", gpr(4)
    #trace "%x", array
    
    add reg(4), reg(0), reg(0) #reset to zero of $4 (temporary hack)
    la reg(4), :array
    $i = 0
    while $i < 12 do
    	lw reg(3), 0, reg(4)    
        #trace "%x", gpr(5)
        trace "%x", gpr(3)
        addi reg(4), reg(4), 4
        
        $i +=1
    end
    end
end