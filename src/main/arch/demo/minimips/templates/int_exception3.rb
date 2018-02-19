#
# Copyright 2017 ISP RAS (http://www.ispras.ru)
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

require_relative 'minimips_base'

#
# Description:
#
# This test template demonstrates how to generate test cases
# for integer arithmetics. This includes situations 'Normal' and 'Overflow'
# extracted from specifications for integer addition and subtraction.
#
# NOTE: This template uses an exception handler defined as a sequence block with
# a fixed origin. Code of such a handler is constructed only after an exception
# occurs for the first time. Simulation of the sequence that raises an exception
# is paused until the handler code is constructed.
#
class IntExceptionTemplate < MiniMipsBaseTemplate

  def pre
    data_config(:target => 'M') {
      # Nothing
    }

    section_text(:pa => 0x0, :va => 0x0) {}
    section_data(:pa => 0x00080000, :va => 0x00080000) {}

    preparator(:target => 'RCOP0') {
      # Nothing
    }

    preparator(:target => 'REG') {
      lui target, value(16, 31)
      ori target, target, value(0, 15)
    }

    comparator(:target => 'REG') {
      prepare at, value
      bne at, target, :check_failed
      nop
    }
  end

  def run
    j :start
    nop

    org 0x380
    sequence {
      trace 'Exception handler (EPC = 0x%x)', location('COP0_R', 14)
      mfc0 ra, rcop0(14)
      addi ra, ra, 4
      jr ra
      nop
    }.run

    org 0x00010000
    label :start

    j :bottom
    nop

    label :top
    block(:combinator => 'product', :compositor => 'random') {
      iterate {
        add t0, t1, t2 do situation('normal') end
        sequence { add t0, t1, t2 do situation('IntegerOverflow') end; nop }
      }

      iterate {
        sub t3, t4, t5 do situation('normal') end
        sequence { sub t3, t4, t5 do situation('IntegerOverflow') end; nop }
      }
    }.run
    j :end
    nop

    org 0x00010100
    label :bottom
    j :top
    nop

    label :end
    nop
  end

end
