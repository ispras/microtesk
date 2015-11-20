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
# by using randomly selected test situations.
#
class RandomSituationTemplate < MiniMipsBaseTemplate

  def pre
    super

    # Start address
    org 0x00020000
  end

  def run
    int32_dist = dist(
      range(:value => 0,                      :bias => 25),
      range(:value => 1..2,                   :bias => 25),
      range(:value => 0xffffFFFE..0xffffFFFF, :bias => 50))

    sit_dist = dist(
      range(:value => situation('add.overflow'), :bias => 20),
      range(:value => situation('add.normal'),   :bias => 20),
      range(:value => situation('zero'),         :bias => 25),
      range(:value => situation('random_biased', :dist => int32_dist), :bias => 35))

    atomic {
      add t1, t2, t3 do random_situation(sit_dist) end
      nop # Place holder to return from exception 
    }.run(10)
  end
end
