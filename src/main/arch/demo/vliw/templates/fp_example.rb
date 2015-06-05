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
# This test template demonstrates the use of data generators 
# for floating-point instructions in MicroTESK.
#
class FpExampleTemplate < VliwBaseTemplate

  def run
    # Adding and subtracting data in random floating-point registers.
    vliw(
      (add_s f(_), f(_), f(_)),
      (sub_s f(_), f(_), f(_))
    )

    # All registers are filled with zeros.
    comment 'zero (:size => 32)'
    vliw(
      (add_s f(1), f(3), f(5)),
      (add_s f(2), f(4), f(6))
    ) do situation('zero', :size => 32) end

    # Random registers are filled with random values.
    vliw(
      (add_s f(_), f(_), f(_)),
      (add_s f(_), f(_), f(_))
    )
  end
end
