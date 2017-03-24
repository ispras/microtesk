#
# Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to use instruction blocks.
#
class BlockTemplate < MiniMipsBaseTemplate

  def run
    org 0x00020000

    # Produces a single test case that consists of three instructions
    sequence {
      Add t0, t1, t2
      Sub t3, t4, t5
      And reg(_), reg(_), reg(_)
    }.run

    # Atomic sequence. Works as sequence in this context.
    atomic {
      Add t0, t1, t2
      Sub t3, t4, t5
      And reg(_), reg(_), reg(_)
    }.run

    # Produces three test cases each consisting of one instruction
    iterate {
      epilogue { nop }
      Add t0, t1, t2
      Sub t3, t4, t5
      And reg(_), reg(_), reg(_)
    }.run

    # Produces four test cases consisting of two instructions
    # (Cartesian product composed in a random order)
    block(:combinator => 'product', :compositor => 'random') {
      epilogue { nop }
      iterate {
        Add t0, t1, t2
        Sub t3, t4, t5
      }

      iterate {
        And reg(_), reg(_), reg(_)
        nop
      }
    }.run

    # Merges two sequnces in random fashion. Atomic sequences are unmodifiable.
    block(:combinator => 'diagonal', :compositor => 'random', :obfuscator => 'random') {
      sequence {
        Add t0, t1, t2
        Sub t3, t4, t5
        Or  t7, t8, t9
      }

      atomic {
        prologue { comment 'Atomic starts' }
        epilogue { comment 'Atomic ends' }

        And reg(_), reg(_), reg(_)
        nop
      }
    }.run
  end

end
