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
# This test template demonstrates how to generate randomized test cases 
# by using biased values, intervals, arrays and distributions. 
#
class RandomTemplate < MiniMipsBaseTemplate
  
  def pre
    super

    #
    # Test case level prologue
    #
    prologue {
      # TODO: align 4
      add zero, zero, zero
    }

    #
    # Test case level epilogue
    #
    epilogue {
      nop
      nop
    }
  end

  def run
    # Predefined probability distribution.
    int32_dist = dist(range(:value => 0,                      :bias => 25),  # Zero
                      range(:value => 1..2,                   :bias => 25),  # Small
                      range(:value => 0xffffFFFE..0xffffFFFF, :bias => 50))  # Large

    1000.times {
      atomic {
        # ADD instruction with biased operand values.
        add t0, t1, t2 do situation('random_biased',
          :dist => dist(range(:value=> int32_dist,              :bias => 80),  # Simple
                        range(:value=> [0xDEADBEEF, 0xBADF00D], :bias => 20))) # Magic
        end
      }
    }
  end
end
