#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# euclid_loop.rb, Sep 23, 2014 11:56:44 AM
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
# algorithm. This template is an extended version of the mips_demo_euclid.rb
# template. It repeats the code of the test program five times to
# demonstrate the use of loops in test templates.  
#

class MipsDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    trace "Euclidean Algorithm (MIPS): Debug Output"

    (1..5).each do |it|
      trace "\n" + "-" * 80

      i = Random.rand(63) + 1 # [1..63], zero is excluded (no solution)
      j = Random.rand(63) + 1 # [1..63], zero is excluded (no solution)

      trace "\nInput parameter values (iteration #{it}): #{i}, #{j}\n"

      addi 4, REG_IND_ZERO(0), IMM16(i)
      addi 5, REG_IND_ZERO(0), IMM16(j)

      trace "\nCurrent register values (iteration #{it}): $4 = %d, $5 = %d\n", gpr(4), gpr(5)

      label :"cycle#{it}"
      beq REG_IND_ZERO(4), REG_IND_ZERO(5), IMM16(:"done#{it}")

      slt 2, REG_IND_ZERO(4), REG_IND_ZERO(5)
      bne REG_IND_ZERO(2), REG_IND_ZERO(0), IMM16(:"if_less#{it}")
      nop

      subu 4, REG_IND_ZERO(4), REG_IND_ZERO(5)
      b IMM16(:"cycle#{it}")
      nop

      label :"if_less#{it}"
      subu 5, REG_IND_ZERO(5), REG_IND_ZERO(4)
      b IMM16(:"cycle#{it}")

      label :"done#{it}"
      move 6, REG_IND_ZERO(4)

      trace "\nResult stored in $6 (iteration #{it}): %d", gpr(6)
    end
  end

  def gpr(index)
    location('GPR', index)
  end

end
