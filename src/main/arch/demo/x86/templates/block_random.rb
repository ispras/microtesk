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
# This test template demonstrates how to create randomized instruction
# sequences using block constructs.
#
class BlockRandomTemplate < X86BaseTemplate

  def run
    instructions = iterate {
      add_r16r16 r16(_), r16(_)
      sub_r16r16 r16(_), r16(_)
      or_r16r16  r16(_), r16(_)
      xor_r16r16 r16(_), r16(_)
      and_r16r16 r16(_), r16(_)
      add_r16i16 r16(_), imm16(1)
      sub_r16i16 r16(_), imm16(2)
      xor_r16i16 r16(_), imm16(3)
    }

    #
    # Constructs 10 instruction sequences which consist of 10 instructions
    # randomly selected from a collection described by the "iterate" construct.
    #
    block(:combinator => 'random') {
      instructions.add 10
    }.run 10
  end

end
