#
# Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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
    # Produces a single test case that consists of three instructions
    atomic {
      Add t0, t1, t2
      Sub t3, t4, t5
      And reg(_), reg(_), reg(_)
    }.run

    # Produces three test cases each consisting of one instruction
    block {
      Add t0, t1, t2
      Sub t3, t4, t5
      And reg(_), reg(_), reg(_)
    }.run
  end

end
