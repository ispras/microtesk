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
# This test template demonstrates how to use instruction groups in test templates.
#
class GroupsTemplate < MiniMipsBaseTemplate
  def run
    # Using groups defined in the specification

    sequence {
      # Selects from {add, addu, sub, subu}
      arithm t0, t1, t2

      # Selects from {and, or, nor, xor}
      bitwise t3, t4, t5

      # Selects from {{add, addu, sub, subu}, {and, or, nor, xor}, {sllv, srav, srlv}}
      alu t6, t7, t8

      # Placeholder to return from an exception
      nop
    }.run(10)

    # Using user-defined groups

    # Probability distribution for instruction names (NOTE: group names are not allowed here)
    xxx_dist = dist(range(:value => 'add',                       :bias => 40),
                    range(:value => 'sub',                       :bias => 30),
                    range(:value => ['and', 'or', 'nor', 'xor'], :bias => 30))

    define_op_group('xxx', xxx_dist)

    # Probability distribution for random values
    int32_dist = dist(range(:value => 0,                         :bias => 25),
                      range(:value => 1..2,                      :bias => 25),
                      range(:value => 0xffffFFFE..0xffffFFFF,    :bias => 50))

    # All instructions of the 'xxx' group will be linked with the 'random_situation'
    # situation unless another situation is explicitly specified.
    set_default_situation 'xxx' do situation('random_biased', :dist => int32_dist) end

    sequence {
      # Selects an instruction according to the 'xxx_dist' distribution
      xxx t0, t1, t2
      xxx t3, t4, t5
      xxx t6, t7, t8

      # Placeholder to return from an exception
      nop
    }.run(10)
  end

end
