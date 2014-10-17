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

require_relative 'vliw_base'

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described program calculates the integer square root
# a positive integer.
#

class IntSqrt4Template < VliwBaseTemplate

  def initialize
    super
    @is_executable = true
  end

  def run
    trace "Integer square root: Debug Output"

    i = Random.rand(1024)

    trace "\nInput parameter value: x = #{i}\n"      
    vliw (addi r(4), r(0), i), (lui r(1), 0x4000)
    vliw (move r(2), r(0)), nop
    
    label :cycle    
    trace "\nCurrent register values: $1 = %d, $2 = %d, $3 = %d\n", gpr(1), gpr(2), gpr(3)

    vliw (beq r(1), r(0), :done), (OR r(3), r(2), r(1))

    vliw (srl r(2), r(2), 1), (slt r(6), r(4), r(3))   
    vliw (bne r(6), r(0), :if_less), nop
    
    vliw (sub r(4), r(4), r(3)), (OR r(2), r(2), r(1))
  
    label :if_less
    vliw (b :cycle), (srl r(1), r(1), 2)
    
    label :done
    trace "\isqrt of #{i} : %d\n", gpr(2)
  end

end
