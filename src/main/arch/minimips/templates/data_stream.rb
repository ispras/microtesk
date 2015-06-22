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
      label :data
      word 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
      label :end
      space 1
    }

    data_stream(:data => 'REG', :index => 'REG') {
      init {
        la index_source, start_label
      }

      read {
        lw data_source, 0, index_source
        addi index_source, zero, 4
      }

      write {
        sw data_source, 0, index_source
        addi index_source, zero, 4
      }
    }
  end

  def run
    
  end

end
