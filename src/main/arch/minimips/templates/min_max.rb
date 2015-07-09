#
# Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to work with data declaration constucts.
# The generated program finds minimum and maximum in a 5-element array
# storing random numbers from 0 to 9. 
#

class MinMaxTemplate < MiniMipsBaseTemplate
 
  def pre
    super

    data {
      org 0x0000FFFF
      label :data
      word rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9)
      label :end
      space 1
    }
  end

  def run
    text  '.text'
    trace '.text'

    trace_data :data, :end

    la t0, :data
    la t1, :end

    lw t2, 0, t0
    add s0, zero, t2 
    add s1, zero, t2

    trace ""
    label :cycle
    addi t0, t0, 4
    beq t0, t1, :done 
    lw t2, 0, t0

    slt t3, t2, s0
    beq t3, zero, :test_max
    nop
    add s0, zero, t2 

    label :test_max
    slt t4, s1, t2
    beq t4, zero, :cycle
    nop
    add s1, zero, t2

    j :cycle
    nop

    label :done
    trace "\ns0(min)=%d, s1(max)=%d", gpr_observer(16), gpr_observer(17)
  end

end
