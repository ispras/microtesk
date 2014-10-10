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
        (addi target, target, value(15, 31))
      )
    }
  end

  def run
    # TODO

    # Another way to generate random immediate values:
    # situation imm_random(min, max).
    comment 'imm_random (:min => 1, :max => 31)'
    vliw(
      (addi r(_), r(_), _ do situation('imm_random', :min => 1, :max => 31) end),
      (addi r(_), r(_), _ do situation('imm_random', :min => 1, :max => 31) end)
    )

    # All registers are filled with zeros.
    comment 'zero (:size => 32)'
    vliw(
      (add r(1), r(3), r(5)),
      (add r(2), r(4), r(6))
    ) do situation('zero', :size => 32) end

    # Random registers are filled with random values.
    comment 'random (:size => 32, :min_imm => 1, :max_imm => 31)'
    vliw(
      (add r(_), r(_), r(_)),
      (add r(_), r(_), r(_))
    ) do situation('random', :size => 32, :min_imm => 1, :max_imm => 31) end

  end

  def gpr(index)
    location('GPR', index)
  end

  def fpr(index)
    location('FPR', index)
  end
end
