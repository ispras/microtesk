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
# This test template demonstrates the use of test situations and 
# data generators in MicroTESK.
#
# Possible syntax styles:
#
# Style 1:
#
# vliw(
#   (addi r(4), r(0), 5  do situation('overflow') end),
#   (addi r(5), r(0), 10 do situation('normal') end)
# )
#
# Style 2:
#
# vliw(
#   addi(r(4), r(0), 5)  do situation('overflow') end,
#   addi(r(5), r(0), 10) do situation('normal') end
# )
#

class VliwDemo < VliwDemoTemplate

  def initialize
    super
    @is_executable = true
  end

  def run
    trace_gpr 3
    trace_gpr 5
    trace_gpr 4
    trace_gpr 6

    vliw(
      (add r(1), r(3), r(5) do situation('add', :case => 'normal', :size => 32) end),
      (add r(2), r(4), r(6) do situation('add', :case => 'overflow', :size => 32) end)
    )

    trace_gpr 1
    trace_gpr 2
  end

end
