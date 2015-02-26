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
# This test template demonstrates how to generate test cases 
# for integer arithmetics. This includes situations
# 'Normal' and 'Overflow' for integer addition and subtraction. 
#
class IntExceptionTemplate < MiniMipsBaseTemplate

  def run
    block(:combinator => 'PRODUCT', :compositor => 'RANDOM') {
      block {
        add t0, t1, t2 do situation('add', :case =>   'normal', :size => 32) end
        add t0, t1, t2 do situation('add', :case => 'overflow', :size => 32) end
      }

      block {
        sub t3, t4, t5 do situation('sub', :case =>   'normal', :size => 32) end
        sub t3, t4, t5 do situation('sub', :case => 'overflow', :size => 32) end
      }
    }
  end

end
