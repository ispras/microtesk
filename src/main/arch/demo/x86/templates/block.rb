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

require_relative 'x86_base'

#
# Description:
#
# This test template demonstrates how to use instruction blocks.
#
class BlockTemplate < X86BaseTemplate

  def run
    # Produces a single test case that consists of three instructions
    sequence {
      mov_r16r16 ax, bx
      sub_r16r16 cx, dx
      add_r16r16 r16(_), r16(_)
    }.run

    # Atomic sequence. Works as sequence in this context.
    atomic {
      mov_r16r16 ax, bx
      add_r16r16 cx, dx
      sub_r16r16 r16(_), r16(_)
    }.run

    # Produces three test cases each consisting of one instruction
    iterate {
      mov_r16r16 ax, bx
      sub_r16r16 cx, dx
      add_r16r16 r16(_), r16(_)
    }.run

    # Produces four test cases consisting of two instructions
    # (Cartesian product composed in a random order)
    block(:combinator => 'product', :compositor => 'random') {
      iterate {
        sub_r16r16 cx, dx
        add_r16r16 ax, bx
      }

      iterate {
        mov_r16r16 ax, bx
        sub_r16r16 r16(_), r16(_)
      }
    }.run

    # Merges two sequnces in random fashion. Atomic sequences are unmodifiable.
    block(:combinator => 'diagonal', :compositor => 'random', :obfuscator => 'random') {
      sequence {
        sub_r16r16 bx, ax
        or_r16r16 cx, dx
      }

      atomic {
        prologue { comment 'Atomic starts' }
        epilogue { comment 'Atomic ends' }

        and_r16r16 r16(_), r16(_)
      }
    }.run
  end

end
