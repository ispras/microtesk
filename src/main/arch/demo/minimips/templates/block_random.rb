#
# Copyright 2016 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to create randomized instruction
# sequences using block constructs.
#
class BlockRandomTemplate < MiniMipsBaseTemplate

  def pre
    super

    #
    # Start address
    #
    org 0x00020000
  end

  def run
    instructions = iterate {
      Or   reg(_), reg(_), reg(_)
      Xor  reg(_), reg(_), reg(_)
      And  reg(_), reg(_), reg(_)
      Ori  reg(_), reg(_), 1
      Xori reg(_), reg(_), 2
      Andi reg(_), reg(_), 3
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
