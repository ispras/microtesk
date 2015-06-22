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
# This test template demonstrates how to use data stream constucts.
#
class DataStreamTemplate < MiniMipsBaseTemplate

  def pre
    super

    data {
      label :data1
      byte 1, 2, 3, 4

      label :data2
      half 0xDEAD, 0xBEEF

      label :data3
      word 0xDEADBEEF

      label :hello
      ascii  'Hello'

      label :world
      asciiz 'World'

      space 6
    }

    data_stream(:data => 'REG', :index => 'REG') {
      init {
         # reg_index = start_label   // User-defined code
      }

      read {
        # reg_data = mem[reg_index] // User-defined code
        # reg_index++
      }

      write {
        # mem[reg_index] = reg_data // User-defined code
        # reg_index++
      }
    }
  end

  def run
    
  end

end
