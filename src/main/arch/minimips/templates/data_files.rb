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
# This test template demonstrates how to generate data files.
#
class DataFilesTemplate < MiniMipsBaseTemplate

  def run
    # Generated data are included in the source file
    generate_data 0x00001000, :data0, 'word', 64, :random, false

    # Generated data are placed to separate files
    generate_data 0x0000F000, :data1, 'word', 64, :random
    generate_data 0x0000FFFF, :data2, 'word', 64, :zero

    org 0x00010000

    trace_memory_data :data0, 64
    la t0, :data0

    trace_memory_data :data1, 64
    la t1, :data1

    trace_memory_data :data2, 64
    la t2, :data2
  end

  def trace_memory_data(begin_label, length)
    trace "Data at #{begin_label}:"
    addr = get_address_of(begin_label)
    length.times.each {
      trace "M[%d]: %d", addr, mem_observer(addr)
      addr = addr + 1
    }
    trace ''
  end

end
