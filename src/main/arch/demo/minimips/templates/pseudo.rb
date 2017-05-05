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
# This test template demonstrates how to use the 'pseudo' construct
# to specify pseudo instruction calls.
#
class PseudoTemplate < MiniMipsBaseTemplate

  def run
    comment "Custom initialization"
    label :start
    pseudo "initialize"
    newline

    add  t0, zero, zero
    addi t1, t0, 5

    label :repeat
    beq t0, t1, :end
    nop
    j :repeat
    addi t0, t0, 1

    newline
    comment "Custom finalization"
    label :end
    pseudo "finalize"
    pseudo "terminate"
  end

end
