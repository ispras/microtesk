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
# This test template demonstrates how to generate test cases 
# for integer arithmetics. This includes situations
# 'Normal' and 'Overflow' for integer addition and subtraction. 
#
class IntExceptionTemplate < VliwBaseTemplate

  def run
    # Prints initial state (after initialization)
    trace_all_gprs

    vliw(
      (add r(1), r(3), r(5) do situation('add', :case => 'normal', :size => 32) end),
      (add r(2), r(4), r(6) do situation('add', :case => 'overflow', :size => 32) end)
    )

    vliw(
      (sub r(7),   r(8),  r(9) do situation('sub', :case => 'normal', :size => 32) end),
      (sub r(10), r(11), r(12) do situation('sub', :case => 'overflow', :size => 32) end)
    )

    # Prints resulting state
    trace_all_gprs
  end

  # Prints values of all involved GPRs
  def trace_all_gprs
    trace ''
    (1..12).each { |index| trace_gpr index }
    trace ''
  end

end
