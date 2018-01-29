#
# Copyright 2018 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to generate test cases for integer
# arithmetics. This includes situations 'Normal' and 'Overflow' extracted from
# specifications for integer addition and subtraction.
#
# It uses the testdata construct to provide random values to be used for the
# first input register. The value of the second register will be generated
# taking into account the value of the first one.
#
class IntExceptionTemplate < MiniMipsBaseTemplate

  def run
    epilogue { nop }

    block(:combinator => 'product', :compositor => 'random') {
      iterate {
        add t0, (t1 do testdata('random') end), t2 do
          situation('normal')
        end

        add t0, (t1 do testdata('random') end), t2 do
          situation('IntegerOverflow')
        end
      }

      iterate {
        sub t3, (t4 do testdata('random') end), t5 do
          situation('normal')
        end

        sub t3, (t4 do testdata('random') end), t5 do
          situation('IntegerOverflow')
        end
      }
    }.run
  end

end
