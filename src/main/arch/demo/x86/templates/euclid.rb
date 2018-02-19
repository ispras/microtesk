#
# Copyright 2017 ISP RAS (http://www.ispras.ru)
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

require_relative 'x86_base'

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described program calculates the greatest common
# divisor of two 5-bit random numbers ([1..63]) by using the Euclidean
# algorithm.
#
class EuclidTemplate < X86BaseTemplate

  def run
    sequence {
      # Values from [1..63], zero is excluded because there is no solution
      val1 = rand(1, 63)
      val2 = rand(1, 63)

      trace "\nInput parameter values: %d, %d\n", val1, val2

      prepare ax, val1
      prepare bx, val2

      label :cycle
      trace "\nCurrent values: ax=%d, bx=%d\n", gpr_observer(0), gpr_observer(3)
      cmp_r16r16 ax, bx
      je :done

      jl :if_less

      sub_r16r16 ax, bx
      jmp_long :cycle

      label :if_less
      sub_r16r16 bx, ax

      jmp_long :cycle

      label :done
      mov_r16r16 cx, ax

      trace "\nResult stored in cx: %d", gpr_observer(1)
    }.run
  end

end
