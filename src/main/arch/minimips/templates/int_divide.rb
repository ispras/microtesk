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
# design under test. The described program calculates the quotient and
# the remainder of division of two random numbers by using 
# the simple algorithm of repeated subtraction.
#

class IntDivideTemplate < MiniMipsBaseTemplate

  def run
    trace "Division: Debug Output"

    dividend = rand(0, 1023)
    divisor  = rand(1, 63) #zero is excluded

    trace "\nInput parameter values: dividend = %d, divisor = %d\n", dividend, divisor

    addi s0, zero, dividend
    addi s1, zero, divisor

    add t0, zero, zero
    add t1, zero, s0

    label :cycle
    trace "\nCurrent register values: $8 = %d, $9 = %d, $10 = %d\n",
      gpr_observer(8), gpr_observer(9), gpr_observer(10)

    sub t2, t1, s1
    slt t3, t2, zero

    bne t3, zero, :done
    nop

    add t1, zero, t2
    addi t0, t0, 1

    j :cycle
    nop

    label :done
    trace "\nResult: quotient ($8) = %d, remainder ($9) = %d",
      gpr_observer(8), gpr_observer(9)
  end

end
