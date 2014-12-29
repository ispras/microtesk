#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to work with data declaration constucts. 
#  

class DataDemoTemplate < MinimipsBaseTemplate
  
  def pre
    super

    data {
      label :data1
      word 1, 2, 3, 4

      label :data2
      half 0xDEAD, 0xBEEF

      label :hello
      ascii  'Hello'

      label :world
      asciiz 'World'

      space 8
    }

  end

  def run
    text '.text'

    trace "%x", mem(0)
    trace "%x", mem(1)
    trace "%x", mem(2)
    trace "%x", mem(3)

    trace "data1: %x", address(:data1)
    trace "data2: %x", address(:data2)
    trace "hello: %x", address(:hello)
    trace "world: %x", address(:world)
    
    la t1, :data2
    trace "%x", gpr(9)
  end

end
