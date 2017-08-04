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

require_relative 'minimips_base'

#
# Description:
#
# This test template demonstrates how to generate random immadiate values.
#
class RandomImmediateTemplate < MiniMipsBaseTemplate
  def pre
    super

    #
    # Start address
    #
    org 0x00020000
  end

  def run
    # Predefined probability distribution
    int16_dist = dist(range(:value => 0,              :bias => 25),  # Zero
                      range(:value => 1..8,           :bias => 25),  # Small
                      range(:value => 0xFFF0..0xFFFF, :bias => 50))  # Large

    # Random value from the given range
    sequence(:presimulation => false) {
      andi reg(_), reg(_), x = rand(0, 31)
      ori  reg(_), reg(_), x
    }.run(10)

    # Random value described by a probability distribution
    sequence(:presimulation => false) {
      andi reg(_), reg(_), x = rand(int16_dist)
      ori  reg(_), reg(_), x
    }.run(10)

    # Unknown value. Set as a random value of the argument's type
    sequence(:presimulation => false) {
      andi reg(_), reg(_), x = _
      ori  reg(_), reg(_), x
    }.run(10)
  end
end
