#
# Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to generate test data on the basis of
# constraints for immediate arguments of instructions.
#
class ConstraintImmediteTemplate < MiniMipsBaseTemplate
  def initialize
    super
    set_option_value 'self-checks', true
  end

  def run
    sequence {
      andi t0, t1, _ do situation('zero') end
      ori  t2, t3, _ do situation('zero') end
      addi t4, t4, _ do situation('IntegerOverflow') end
      nop
    }.run
  end
end
