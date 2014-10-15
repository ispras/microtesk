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

require_relative 'base_template'

#
# Description:
#
# This test template demonstrates the use of data generators 
# for floating-point instructions in MicroTESK.
#
class VliwDemo < VliwDemoTemplate

  def initialize
    super
    @is_executable = true
  end

  def run
    # Adding and subtracting data in random floating-point registers.
    comment 'imm_random (:min => 1, :max => 31)' 
    vliw(
      (add_s f(_), f(_), f(_)),
      (sub_s f(_), f(_), f(_))
    ) do situation('imm_random', :min => 1, :max => 31) end

    # All registers are filled with zeros.
    comment 'zero (:size => 32)'
    vliw(
      (add_s f(1), f(3), f(5)),
      (add_s f(2), f(4), f(6))
    ) do situation('zero', :size => 32) end

    # Random registers are filled with random values.
    comment 'random (:size => 32, :min_imm => 1, :max_imm => 31)'
    vliw(
      (add_s f(_), f(_), f(_)),
      (add_s f(_), f(_), f(_))
    ) do situation('random', :size => 32, :min_imm => 1, :max_imm => 31) end
  end

end
