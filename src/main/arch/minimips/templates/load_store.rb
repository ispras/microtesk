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
# This test template demonstrates how to work with data definition constucts
# and load and store instructions.
#
class LoadStoreTemplate < MiniMipsBaseTemplate

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
  end

  def run
    text '.text'

    trace_memory
    trace_labels

    trace_header 'Reading from memory:'

    la t0, :data1
    trace "address in t0: %x", gpr(8)
    lw t1, 0, t0
    trace "value in t1: %x", gpr(9)

    la t2, :data2
    trace "address in t2: %x", gpr(10)
    lw t3, 0, t2
    trace "value in t3: %x", gpr(11)

    la t4, :data3
    trace "address in t4: %x", gpr(12)
    lw t5, 0, t4
    trace "value in t5: %x", gpr(13)

    trace_header 'Writing to memory:'

    sw t5, 0, t0 
    sw t1, 0, t4 

    trace_memory
  end

  def trace_memory
    trace_header 'Memory state:'
    (0..6).each { |i| trace "%x: %x", i * 4, mem(i) }
  end

  def trace_labels
    trace_header 'Labels:'
    trace "data1 address: %x", address(:data1)
    trace "data2 address: %x", address(:data2)
    trace "data3 address: %x", address(:data3)
    trace "hello address: %x", address(:hello)
    trace "world address: %x", address(:world)
  end

  def trace_header(header_text)
    trace '-' * 80
    trace header_text
    trace ''
  end

end
