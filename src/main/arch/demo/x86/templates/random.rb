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
# This test template demonstrates how to generate randomized test cases
# by using biased values, intervals, arrays and distributions.
#
class RandomTemplate < X86BaseTemplate
  def run
    # Predefined probability distribution.
    int16_dist = dist(range(:value => 0,              :bias => 25),  # Zero
                      range(:value => 1..2,           :bias => 25),  # Small
                      range(:value => 0xFFFE..0xFFFF, :bias => 50))  # Large

    sequence {
      # ADD instruction with biased operand values.
      add_r16r16 ax, dx do situation('random_biased',
        :dist => dist(range(:value=> int16_dist,      :bias => 80),  # Simple
                      range(:value=> [0xBEEF, 0xF0D], :bias => 20))) # Magic
      end
    }.run(1000)
  end
end
