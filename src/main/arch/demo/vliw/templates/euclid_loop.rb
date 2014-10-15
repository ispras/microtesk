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

require ENV['TEMPLATE']

#
# Description:
#
# This test template demonstrates how MicroTESK can simulate the execution
# of a test program to predict the resulting state of a microprocessor
# design under test. The described program calculates the greatest common
# divisor of two 5-bit random numbers ([1..63]) by using the Euclidean 
# algorithm. This template is an extended version of the euclid.rb
# template. It repeats the code of the test program five times to
# demonstrate the use of loops in test templates.  
#

class VliwDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    trace "Euclidean Algorithm: Debug Output"

    (1..5).each do |it|
      trace "\n" + "-" * 80

      # Values from [1..63], zero is excluded because there is no solution
      i = Random.rand(63) + 1
      j = Random.rand(63) + 1

      trace "\nInput parameter values (iteration #{it}): #{i}, #{j}\n"
      vliw (addi r(4), r(0), i), (addi r(5), r(0), j)

      label :"cycle#{it}"
      trace "\nCurrent register values (iteration #{it}): $4 = %d, $5 = %d\n", gpr(4), gpr(5)
      vliw (beq r(4), r(5), :"done#{it}"), (move r(6), r(4))

      vliw (slt r(2), r(4), r(5)), nop
      vliw (bne r(2), r(0), :"if_less#{it}"), nop

      vliw (b :"cycle#{it}"), (sub r(4), r(4), r(5))

      label :"if_less#{it}"
      vliw (b :"cycle#{it}"), (sub r(5), r(5), r(4))

      label :"done#{it}"
      trace "\nResult stored in $6 (iteration #{it}): %d", gpr(6)
    end
  end

  def gpr(index)
    location('GPR', index)
  end

end
