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

class IntSqrtTemplate < VliwBaseTemplate

  def run
    trace "Integer square root: Debug Output"

    x = rand(0, 1023)

    trace "\nInput parameter value: x = %d\n", x
    vliw (addi r(3), r(0), x), (addi r(1), r(0), 1)
    vliw (move r(2), r(0)),    (addi r(4), r(0), 1)

    label :cycle
    trace "\nCurrent register values: $1 = %d, $2 = %d, $3 = %d\n", gpr(1), gpr(2), gpr(3)

    vliw (slt r(6), r(0),  r(3)), nop
    vliw (beq r(6), r(0), :done), nop

    vliw (sub  r(3), r(3), r(1)), nop
    vliw (addi r(1), r(1),    2), nop

    vliw (slt r(6), r(3), r(0)), nop
    vliw (sub r(5), r(4), r(6)), nop
    vliw (add r(2), r(2), r(5)), nop

    vliw (b :cycle), nop

    label :done
    trace "\nsqrt of %d: %d", x, gpr(2)
  end

end
