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

require ENV['TEMPLATE']

#
# Description:
#
# The purpose of the CpuDemoTemplate is to demonstrate how code of test
# templates can be reused by other test templates. The class provides
# implementations of the 'pre' and 'post' methods that are the initialization
# and finalization sections respectively. These methods can be reused by other
# test templates using the mechanism of class inheritance.
#
class CpuBaseTemplate < Template

  #
  # Initialization section. Contains code to be inserted in the beginning of
  # of a test case. It also contains descriptions of preparators. A preparator
  # is a rule used by test data generators to creation initialization
  # sequences of instructions. This sequences assign the generated values to
  # corresponding registers and addresses and are inserted in the beginning of
  # the test program.
  #
  def pre
    # Physical memory is modelled by array M.
    data_config(:target => 'M') {
      # Data type definitions must be placed here.
    }

    #
    # Defines .text section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_text(:pa => 0x0, :va => 0x0) {}

    #
    # Defines .data section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_data(:pa => 0x0, :va => 0x0) {}

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified general-purpose register (GPR) using the REG addressing
    # mode.
    #
    preparator(:target => 'REG') {
      comment 'Initializer for REG: %d', value

      mov target, imm(value(2, 7))
      add target, target
      add target, target
      add target, imm(value(0, 1))
    }

    preparator(:target => 'REG', :mask => "'b00XX_XXXX") {
      comment 'Initializer for REG: %d', value
      mov target, imm(value(0, 5))
    }

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified memory address using the MEM addressing mode.
    #
    preparator(:target => 'MEM') {
      comment 'Initializer for MEM: %d', value

      mov target, imm(value(2, 7))
      add target, target
      add target, target
      add target, imm(value(0, 1))
    }

    preparator(:target => 'IMM') {
    }

    org 0x10

    trace 'Initialization:'
    comment 'Initialization Section Starts'
    mov mem(:i => 12), imm(0x0F)
    comment 'Initialization Section Ends'
  end

  #
  # Finalization section. Contains code to be inserted in the end
  # of a test case.
  #
  def post
    trace 'Finalization:'
    comment 'Finalization Section Starts'
    mov mem(:i => 23), imm(23)
    comment 'Finalization Section Ends'
  end

end
