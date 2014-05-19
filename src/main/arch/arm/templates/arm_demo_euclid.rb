#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# arm_demo_euclid.rb, Apr 15, 2014 2:24:13 PM
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
# algorithm.   
#

class ArmDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    trace "Euclidean Algorithm: Debug Output\n"

    i = Random.rand(63) + 1 # [1..63], zero is excluded (no solution) 
    j = Random.rand(63) + 1 # [1..63], zero is excluded (no solution)

    trace "\nInput parameter values: #{i}, #{j}\n\n"

    EOR           blank, setsOff, REG(0), REG(0), register0
    ADD_IMMEDIATE blank, setsOff, REG(0), REG(0), IMMEDIATE(0, i)

    EOR           blank, setsOff, REG(1), REG(1), register1
    ADD_IMMEDIATE blank, setsOff, REG(1), REG(1), IMMEDIATE(0, j)

    trace '"\nInitial register values: R0 = #{getGPR(0)}, R1 = #{getGPR(1)}\n\n"'

    label :cycle

    CMP blank, REG(0), register1
    SUB greaterThan, setsOff, REG(0), REG(0), register1
    SUB lessThan,    setsOff, REG(1), REG(1), register0

    trace '"\nCurrent register values: R0 = #{getGPR(0)}, R1 = #{getGPR(1)}\n\n"' 

    B notEqual, :cycle

    MOV blank, setsOff, REG(2), register0

    trace '"\nResult stored in R2: #{getGPR(2)}\n"'
  end

  def getGPR(index)
    get_loc_value('GPR', index).to_s 
  end

end
