#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# arm_demo_labels.rb, Aug 10, 2014 1:03:05 PM
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

class ArmDemo < Template
  def initialize
    super
    @is_executable = true
  end

  def run

    sub lessThan, setsoff, REG(9), reg(0), register0

    label :valiant

    add_immediate blank, setsOff, REG(0), REG(0), IMMEDIATE(0, 1)
    cmp_immediate blank, reg(0), immediate(0, 5)

    # Prints data stored in all GPR registers
    trace "\nDEBUG: GRP values:"
    (0..15).each { |i| trace "GRP[%d] = %s", i, location("GPR", i) }
    trace ""

    b notEqual, :valiant
    
    block {

      b blank, :compliant

      sub blank, setsoff, reg(10), reg(10), register0

      label :compliant

      sub blank, setsoff, reg(11), reg(0), register0

      sub blank, setsoff, reg(13), reg(0), register0

      b blank, :valiant

      sub blank, setsoff, reg(14), reg(0), register0

      label :valiant

      sub blank, setsoff, reg(15), reg(0), register0

      b blank, :defiant

      sub blank, setsoff, reg(12), reg(0), register0
    }

    label :defiant

    sub blank, setsoff, reg(1), reg(1), register1
    add_immediate blank, setsoff, reg(1), reg(1), immediate(0, 1)

    #b notequal, :defiant
    #b blank, :defiant

    #add_immediate blank, setsoff, reg(2), reg(2), immediate(0, 2)
    sub blank, setsoff, reg(0), reg(0), register0

    sub blank, setsoff, reg(1), reg(1), register1
    #add_immediate blank, setsoff, reg(3), reg(3), immediate(0, 3)

  end
end
