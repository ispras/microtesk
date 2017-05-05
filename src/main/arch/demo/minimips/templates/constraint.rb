#
# Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to generate test cases on the basis of
# constraints.
#
class ConstraintTemplate < MiniMipsBaseTemplate
  def run
    org 0x00020000
    sequence {
      add t0, t1, t2 do situation('constraint_int_overflow') end
      nop
      nop
    }.run(100)
  end
end
