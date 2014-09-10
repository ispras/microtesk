#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# vliw_demo.rb, May 23, 2014 11:37:53 AM Andrei Tatarnikov
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

class VLIWDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    comment "This a demo template for a simple VLIW ISA based on MIPS"

    # Full syntax that uses the full hierarchy of operations.

    LongWord_ALU(
      ALUWords1(
        alu(add(reg(3), reg(1), reg(2))),
      )
    )

    LongWord_ALU(
      ALUWords2(
        alu(add(reg(3), reg(1), reg(2))),
        alu(sub(reg(3), reg(1), reg(2)))  
      )
    )

    LongWord_ALU(
      ALUWords3(
        alu(add(reg(3), reg(1), reg(2))),
        alu(sub(reg(3), reg(1), reg(2))),
        alu(addi(reg(3), reg(1), imm16(10)))  
      )
    )

    (1..3).each do |i|
      trace "GPR[#{i}] = %s", location("GPR", i)
    end

    # Reduced syntax that uses shortcuts
    # (the intermediate operation ALU is skipped).

    LongWord_ALU(
      ALUWords1(
        add(reg(3), reg(1), reg(2)),
      )
    )

    LongWord_ALU(
      ALUWords2(
        add(reg(3), reg(1), reg(2)),
        sub(reg(3), reg(1), reg(2))  
      )
    )

    LongWord_ALU(
      ALUWords3(
        add(reg(3), reg(1), reg(2)),
        sub(reg(3), reg(1), reg(2)),
        addi(reg(3), reg(1), imm16(10))  
      )
    )

    (1..3).each do |i|
      trace "GPR[#{i}] = %s", location("GPR", i)
    end

  end

end
