#
# Copyright 2016 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to use external code sections and labels.
# External code is code defined outside blocks describing test cases. It can contain
# labels. These labels are visible from other external code sections and can be used
# to perform control transfers in a backward direction (to earlier generate code).
#
class ExternalLabelsTemplate < MiniMipsBaseTemplate

  def run
    prepare s0, 5 # s0 = 5
    label :start
    nop

    sequence(:presimulation => false) {
      add t0, t1, t2
      sub t3, t4, t5
      addi s0, s0, 1 # s0 = s0 + 1
    }.run

    ori t0, zero, 1
    sub s0, s0, t0 # s0 = s0 - 1

    sequence(:presimulation => false) {
      data {
        label :data
        word 1
      }

      la t0, :data
      lw t1, 0, t0
      sub s0, s0, t1 # s0 = s0 - 1
    }.run

    bne zero, s0, :start # until s0 != 0
    nop
  end

end
