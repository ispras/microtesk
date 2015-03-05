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
# This test template demonstrates how to generate test cases 
# based on random values. 
#
class RandomTemplate < MiniMipsBaseTemplate

  def run
    my_dist = dist(range(:value => 1,         :bias => 1),
                   range(:value => 1..3,      :bias => 2),
                   range(:value => [1, 2, 3], :bias => 3))

    add t0, t1, t2 do situation('random_biased', :size => 32,
      :dist => dist(range(:value=> 1,         :bias => 1), # Single
                    range(:value=> 1..3,      :bias => 2), # Interval
                    range(:value=> [1, 2, 3], :bias => 3), # Collection
                    range(:value=> my_dist,   :bias => 4)) # Distribution
      ) end
  end

end
