#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# datagen_add.rb, Oct 13, 2014 6:01:42 PM Andrei Tatarnikov
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
        (lui  target, value(0, 15)),
        nop
      )
      print_all_registers
      vliw(
        (addi target, target, value(15, 31)),
        nop
      )
      print_all_registers
    }
  end

  def run
    print_all_registers
    vliw(
      (add r(1), r(3), r(5) do situation('add', :case => 'normal', :size => 32) end),
      (add r(2), r(4), r(6) do situation('add', :case => 'overflow', :size => 32) end)
    )
  end

  def print_all_registers
    trace "\nDEBUG: GPR values:"
    (0..31).each { |i| trace "GPR[%d] = %s", i, location("GPR", i) }
    trace ""
  end

  def gpr(index)
    location('GPR', index)
  end
end
