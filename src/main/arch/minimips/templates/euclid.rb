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

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described program calculates the greatest common
# divisor of two 5-bit random numbers ([1..63]) by using the Euclidean 
# algorithm.
#  

class EuclidTemplate < MinimipsBaseTemplate

  def run
    trace "Euclidean Algorithm (miniMIPS): Debug Output"

    # Values from [1..63], zero is excluded because there is no solution
    x = Random.rand(63) + 1
    y = Random.rand(63) + 1

    trace "\nInput parameter values: #{x}, #{y}\n"

    addi reg(4), reg(0), x
    addi reg(5), reg(0), y

    label :cycle
    trace "\nCurrent register values: $4 = %d, $5 = %d\n", gpr(4), gpr(5)
    beq reg(4), reg(5), :done

    slt reg(2), reg(4), reg(5)
    bne reg(2), reg(0), :if_less
    sll reg(0), reg(0), reg(0) # nop (miniMIPS idiom)

    subu reg(4), reg(4), reg(5)
    j :cycle
    sll reg(0), reg(0), reg(0) # nop (miniMIPS idiom)

    label :if_less
    subu reg(5), reg(5), reg(4)
    j :cycle

    label :done
    add reg(6), reg(4), reg(0)

    trace "\nResult stored in $6: %d", gpr(6)
  end

end
