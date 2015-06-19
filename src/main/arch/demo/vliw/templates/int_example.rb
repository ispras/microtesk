#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

require_relative 'vliw_base'

#
# Description:
#
# This test template demonstrates the use of test situations and 
# data generators in MicroTESK (generating integer values).
#
class IntExampleTemplate < VliwBaseTemplate

  def run
    # Random immediate values: rand(min, max)
    vliw(
      (addi r(rand(1, 31)), r(0), rand(0, 31)),
      (addi r(rand(1, 31)), r(0), rand(0, 31))
    )

    # All registers are filled with zeros.
    vliw(
      (add r(1), r(3), r(5)),
      (add r(2), r(4), r(6))
    ) do situation('zero') end

    # Random registers are filled with random values.
    vliw(
      (add r(_), r(_), r(_)),
      (add r(_), r(_), r(_))
    ) do situation('random') end

    # Random registers are assigned radom values from the specified range.
    # The immediate values are also radom values from the specified range.
    vliw(
      (addi r(_), r(_), _ do situation('random', :min => 1, :max => 31) end),
      (addi r(_), r(_), _ do situation('random', :min => 1, :max => 31) end)
    )
  end

end
