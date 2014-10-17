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
# design under test. The described program calculates the quotient and
# the remainder of division of two random numbers by using 
# the simple algorithm of repeated subtraction.
#

class IntDivideTemplate < VliwBaseTemplate

  def initialize
    super
    @is_executable = true
  end

  def run
    trace "Division: Debug Output"

    i = Random.rand(1024)
    j = Random.rand(63) + 1 #zero is excluded

    trace "\nInput parameter values: dividend = #{i}, divisor = #{j}\n"
    vliw (addi r(4), r(0), i), (addi r(5), r(0), j)
    vliw (move r(1), r(0)),    (move r(2), r(4))

    label :cycle
    trace "\nCurrent register values: $1 = %d, $2 = %d, $3 = %d, $4 = %d, $5 = %d\n", gpr(1), gpr(2), gpr(3), gpr(4), gpr(5)
    
    vliw (sub r(3), r(2), r(5)),  nop
    vliw (slt r(6), r(3), r(0)),  nop
    vliw (bne r(6), r(0), :done), nop
    
    vliw (move r(2), r(3)), (addi r(1), r(1), 1)
    vliw (b :cycle), nop
    
    label :done
    trace "\nResult : quotient = %d, remainder = %d\n", gpr(1), gpr(2)
  end

end
