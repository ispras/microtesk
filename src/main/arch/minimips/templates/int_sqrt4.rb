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
# design under test. The described program calculates the integer square root
# a positive integer.
#

class IntSqrt4Template < MiniMipsBaseTemplate

  def run
    trace "Integer square root: Debug Output\n"

    x = rand(0, 1023)
    trace "Input parameter value: x = %d\n", x

    addi s0, zero, x

    addi s1, zero, 1
    addi s2, zero, 2

    add t3, zero, s0
    lui t0, 0x4000
    add t1, zero, zero

    label :cycle
    trace "\nCurrent register values: $8 = %d, $9 = %d, $10 = %d\n",
      gpr_observer(8), gpr_observer(9), gpr_observer(10)

    beq t0, zero, :done
    OR  t2, t1, t0

    srlv t1, t1, s1
    slt  t4, t3, t2

    bne t4, zero, :if_less
    nop

    sub t3, t3, t2
    OR  t1, t1, t0

    label :if_less
    j :cycle
    srlv t0, t0, s2

    label :done
    trace "\nInteger square root of %d: %d", x, gpr_observer(9)
  end

end
