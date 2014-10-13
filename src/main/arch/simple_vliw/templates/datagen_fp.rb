#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# datagen_fp.rb, Oct 10, 2014 4:45:48 PM Andrei Tatarnikov
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
# This test template demonstrates the use of data generators 
# for floating-point instructions in MicroTESK.
#
class VLIWDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def pre
    #
    # Rules for writing preparators of initializing instruction sequences:
    #
    # preparator(:target => '<name>') {
    #   comment 'Initializer for <name>'
    #   vliw(
    #     (lui  target, value(0, 15)),
    #     (addi target, target, value(15, 31))
    #   )
    # }
    #
    # The above code creates an instruction sequence that writes a value
    # to the resource referenced via the <name> addressing mode.
    #
    # Keywords:
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
        (addi target, target, value(15, 31))
      )
    }

    preparator(:target => 'F') {
      comment 'Initializer for F'
      # GPR[25] holds a temporary value
      vliw(
        (lui  r(25), value(0, 15)),
        (addi r(25), r(25), value(15, 31))
      )
      vliw(
        (mtf r(25), target),
        nop
      )
    }
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

  def gpr(index)
    location('GPR', index)
  end

  def fpr(index)
    location('FPR', index)
  end
end
