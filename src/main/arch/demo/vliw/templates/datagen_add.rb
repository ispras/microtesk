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

require ENV['TEMPLATE']

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
class VLIWDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def pre
    #
    # Creates an instruction sequence that writes a value (32-bit integer) to
    # the resource referenced via the 'r' addressing mode (register GPR).
    #
    # Format:
    # - The ':target' attribute specify the name of the target addressing mode.
    # - The 'target' and 'value' methods specify the target addressing mode
    #   with all its arguments set and the value passed to the preparator
    #   respectively. The arguments of the 'value' method specify which part
    #   of the value is used. 
    #
    preparator(:target => 'R') {
      comment 'Initializer for R'  
      vliw(
        (lui target, value(16, 31)),
        (addi target, target, value(0, 15))
      )
    }
  end

  def run
    print_gpr 3
    print_gpr 5
    print_gpr 4
    print_gpr 6

    vliw(
      (add r(1), r(3), r(5) do situation('add', :case => 'normal', :size => 32) end),
      (add r(2), r(4), r(6) do situation('add', :case => 'overflow', :size => 32) end)
    )

    print_gpr 1
    print_gpr 2
  end

  def print_gpr(index)
    trace "GPR[%d] = %s", index, location('GPR', index)
  end

  def gpr(index)
    location('GPR', index)
  end
end
