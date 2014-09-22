#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# mips_demo.rb, Sep 22, 2014 2:29:28 PM
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
# Simple demo template for MIPS
#

class MipsDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    comment "MIPS TEST"

    # "Plain" sequence
    add 3, REG_IND_ZERO(1), REG_IND_ZERO(2)
    sub 3, REG_IND_ZERO(1), REG_IND_ZERO(2)

    addi 3, REG_IND_ZERO(0), IMM16(0x1)
    addi 3, REG_IND_ZERO(3), IMM16(0x1)

    # Randomized sequence
    block(:compositor => "RANDOM", :combinator => "RANDOM") {
      add 3, REG_IND_ZERO(1), REG_IND_ZERO(2)
      sub 3, REG_IND_ZERO(1), REG_IND_ZERO(2)

      addi 3, REG_IND_ZERO(0), IMM16(0x1)
      addi 3, REG_IND_ZERO(3), IMM16(0x1)
    }

    print_all_registers
  end

  def print_all_registers
    trace "\nDEBUG: GRP values:"
    (0..31).each { |i| trace "GRP[%d] = %s", i, location("GPR", i) }
    trace ""
  end

end
