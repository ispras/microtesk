#
# Copyright 2015 ISP RAS (http://www.ispras.ru)
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
# The template demonstrates how indirect jumps  (jumps to an address stored
# in a register) work.
#
class JumpTemplate < MiniMipsBaseTemplate

  def run
    addi t1, zero, 1

    la t2, :end
    addi t2, t2, 8

    # Jump to the last nop is expected
    jr t2
    nop

    addi t1, t1, 3

    label :end
    add t3, zero, t1
    add t4, zero, t1

    (9..12).each { |i|
      trace "$#{i} = %d", gpr_observer(i)
    }

    nop
  end

end

