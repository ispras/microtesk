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
# This test template demonstrates how to create various combinations 
# of instructions.
#

class CombinationTemplate < VliwBaseTemplate

  def run
    # Randomized sequence
    block(:combinator => 'PRODUCT', :compositor => 'RANDOM') {
      block {
        vliw(
          (add r(1), r(3), r(5) do situation('add', :case => 'normal',   :size => 32) end),
          (add r(2), r(4), r(6) do situation('add', :case => 'overflow', :size => 32) end)
        )

        vliw(
          (sub r(7),   r(8),  r(9) do situation('sub', :case => 'normal',   :size => 32) end),
          (sub r(10), r(11), r(12) do situation('sub', :case => 'overflow', :size => 32) end)
        )
      }

      block {
        vliw(
          (add_s f(5), f(5), f(6) do situation('fp.add', :case => 'underflow', :exp => 8, :frac => 23) end),
          (add_s f(7), f(7), f(8) do situation('fp.add', :case => 'inexact',   :exp => 8, :frac => 23) end)
        )

        vliw(
          (sub_s  f(9),  f(9), f(10) do situation('fp.sub', :case => 'normal',   :exp => 8, :frac => 23) end),
          (sub_s f(11), f(11), f(12) do situation('fp.sub', :case => 'overflow', :exp => 8, :frac => 23) end)
        )
      }
    }
  end

end
