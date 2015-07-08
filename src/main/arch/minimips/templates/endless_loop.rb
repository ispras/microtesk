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
# This test template demonstrates how MicroTESK handles endless loops in test
# templates. If an endless loop is detected, MicroTESK halts generating a test
# program. A template is considered containing an endless loop, if the number of
# times an instruction call is executed exceeds the allowed limit.
#

class EndlessLoopTemplate < MiniMipsBaseTemplate

  def run
    trace "Loop Begins"

    add  t0, zero, zero
    addi t1, zero, 99

    label :repeat
    trace "Iteration: %d", gpr_observer(8)

    # The line below contains a mistake that results in an endless loop.
    slt t2, t2, t1
    bne t2, zero, :repeat
    addi t0, t0, 1

    trace "Loop Ends"
  end

end
