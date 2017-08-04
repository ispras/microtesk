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
# This test template demonstrates how to generate random immediate values.
#
class RandomImmediateTemplate < X86BaseTemplate
  def run
    # Predefined probability distribution
    int16_dist = dist(range(:value => 0,              :bias => 25),  # Zero
                      range(:value => 1..8,           :bias => 25),  # Small
                      range(:value => 0xFFF0..0xFFFF, :bias => 50))  # Large

    # Random value from the given range
    sequence(:presimulation => false) {
      and_r16i16 r16(_), IMM16(x = rand(0, 15))
      or_r16i16  r16(_), IMM16(x)
    }.run(10)

    # Random value described by a probability distribution
    sequence(:presimulation => false) {
      add_r16i16 r16(_), IMM16(x = rand(int16_dist))
      or_r16i16  r16(_), IMM16(x)
    }.run(10)

    # Unknown value. Set as a random value of the argument's type
    sequence(:presimulation => false) {
      add_r16i16 r16(_), IMM16(x = _)
      sub_r16i16 r16(_), IMM16(x)
    }.run(10)
  end
end
