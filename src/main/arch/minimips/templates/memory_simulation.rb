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
# This test template demonstrates how MicroTESK memory simulator works.
#
class MemorySimulationTemplate < MiniMipsBaseTemplate

  def initialize
    super

    # Memory-related settings
    @base_virtual_address = 0x00001000
    @base_physical_address = 0x000FFFF
  end

  def pre
    super
  
    data {
      org 0x00010001
      align 8
      label :data
      word rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9),
           rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9), rand(0, 9)
      label :end
      space 1
    }
  end

  def run
    trace_data :data, :end

    lw s0, 0, t0
    lw s1, 0, t1
  end

end
