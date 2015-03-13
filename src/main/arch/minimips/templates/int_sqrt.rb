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

class IntSqrtTemplate < MiniMipsBaseTemplate

  def run
    trace "Integer square root: Debug Output\n"

    x = rand(0, 1023)
    trace "Input parameter value: x = %d\n", x

    addi s0, zero, x

    add  t0, zero, s0
    addi t1, zero, 1

    add  t2, zero, zero
    addi t3, zero, 1

    label :cycle
    trace "\nCurrent register values: $8 = %d, $9 = %d, $10 = %d\n", gpr(8), gpr(9), gpr(10)

    slt t4, zero, t0
    beq t4, zero, :done
    nop

    sub  t0, t0, t1
    addi t1, t1, 2

    slt t4, t0, zero
    sub t5, t3, t4
    add t2, t2, t5

    j :cycle
    nop

    label :done
    trace "\nInteger square root of %d: %d", x, gpr(10)
  end

end
