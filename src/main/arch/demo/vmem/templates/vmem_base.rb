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

require ENV['TEMPLATE']

class VmemBaseTemplate < Template

  def pre
    # Physical memory is modelled by array MEM.
    data_config(:target => 'MEM') {
      # Data type definitions must be placed here.
    }

    #
    # Defines .text section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_text(:pa => 0x2000, :va => 0xe000) {}

    #
    # Defines .data section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_data(:pa => 0x0100, :va => 0xc100) {}

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified general-purpose register (GPR) using the REG addressing
    # mode.
    #
    preparator(:target => 'REG') {
      comment 'Initializer for REG: %x', value
      ml target, value(0, 7)
      mh target, value(8, 15)
    }

    trace 'Initialization:'
  end

  def post
    trace 'Finalization:'
  end

end
