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

    addi t1, zero, x
    addi t2, zero, y

    label :cycle
    trace "\nCurrent values: $t1($9)=%d, $t2($10)=%d\n", gpr(9), gpr(10)
    beq t1, t2, :done

    slt t0, t1, t2
    bne t0, zero, :if_less
    sll zero, zero, 0 # means 'nop' (idiom)

    subu t1, t1, t2
    j :cycle
    sll zero, zero, 0 # means 'nop' (idiom)

    label :if_less
    subu t2, t2, t1
    j :cycle

    label :done
    add t3, t1, zero

    trace "\nResult stored in $t3($11): %d", gpr(11)
  end

end
