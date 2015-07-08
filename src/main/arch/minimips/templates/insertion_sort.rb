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
# design under test. The described test program is an implemention of 
# the insertion sort algorithm. The algorithm in pseudocode (from Wikipedia):
#
# for i = 1 to length(A) - 1
#   x = A[i]
#   j = i
#   while j > 0 and A[j-1] > x
#     A[j] = A[j-1]
#     j = j - 1
#   A[j] = x
#
class InsertionSortTemplate < MiniMipsBaseTemplate
  def pre
    super

    data {
      label :data
      word rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9),
           rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)
      label :end
      space 1
    }
  end

  def run
    text  '.text'
    trace '.text'

    trace_data :data, :end

    la s0, :data
    la s1, :end
    addi s2, zero, 4

    add t0, s0, s2
    ########################### Outer loop starts ##############################
    label :for
    beq t0, s1, :exit_for

    add t1, zero, t0
    lw s3, 0, t0
    ########################### Inner loop starts ##############################
    label :while
    beq t1, s0, :exit_while

    sub t3, t1, s2
    lw s4, 0, t3
    slt t2, s3, s4

    beq t2, zero, :exit_while
    nop

    sw s4, 0, t1

    j :while
    sub t1, t1, s2
    ############################ Inner loop ends ###############################
    label :exit_while

    sw s3, 0, t1

    j :for
    add t0, t0, s2
    ############################ Outer loop ends ###############################
    label :exit_for

    trace_data :data, :end
  end

end
