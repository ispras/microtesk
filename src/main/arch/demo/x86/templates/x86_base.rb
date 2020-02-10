#
# Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

class X86BaseTemplate < Template

  ################################################################################################

  def i386_assembler
    true
  end

  ################################################################################################

  def initialize
    super

    # Sets the comment token used in test programs
    if is_rev('I80386_GNU') then
      set_option_value 'comment-token', '#'
    else
      set_option_value 'comment-token', ';'
    end

    # Sets the indentation token used in test programs
    set_option_value 'indent-token', "\t"

    # Sets the token used in separator lines printed into test programs
    set_option_value 'separator-token', '='

    # Adds custom text for the header/footer of test programs
    if is_rev('I80386_GNU') then
      add_to_header '.code16'
      add_to_footer '.org 510'
      add_to_footer '.word 0xaa55'
    end

    if is_rev('I80386') then
      add_to_header 'bits 16'
      #add_to_footer 'times 510 - ($ - $$) db 0'
      add_to_footer 'dw 0xAA55'
    end
  end

  ##################################################################################################
  # Prologue
  ##################################################################################################

  def pre
    ################################################################################################

    #
    # Information on data types to be used in data sections.
    #
    data_config(:target => 'MEM') {
      define_type   :id => :byte,   :text => 'db',      :type => type('card', 8)
      define_type   :id => :word,   :text => 'dw',      :type => type('card', 16)
      define_space  :id => :space,  :text => '.space',  :fill_with => 0
      define_string :id => :ascii,  :text => '.ascii',  :zero_term => false
      define_string :id => :asciiz, :text => '.asciiz', :zero_term => true
    }

    #
    # Defines .boot section (contains the magic bytes).
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    section(:name => '.boot', :pa => 0x01fe, :va => 0x01fe, :prefix => is_rev('I80386_GNU') ? '' : 'section') {}

    #
    # Defines .text section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_text(:pa => 0x7c00, :va => 0x7c00, :prefix => is_rev('I80386_GNU') ? '' : 'section') {}

    #
    # Defines .data section.
    #
    # pa: base physical address (used for memory allocation).
    # va: base virtual address (used for encoding instructions that refer to labels).
    #
    section_data(:pa => 0x8000, :va => 0x8000, :prefix => is_rev('I80386_GNU') ? '' : 'section') {}

    ################################################################################################

    #
    # The code below specifies an instruction sequence that writes a value
    # to the specified register (target) via the R addressing mode.
    #
    # Default preparator: It is used when no special case previded below
    # is applicable.
    #
    preparator(:target => 'R16') {
      mov_r16i16  target, IMM16(value(0, 15))
    }

    preparator(:target => 'RSEG16') {
    }
    ################################################################################################

    ################################################################################################

    # The code below specifies a comparator sequence to be used in self-checking tests
    # to test values in the specified register (target) accessed via the REG
    # addressing mode.
    #
    # Comparators are described using the same syntax as in preparators and can be
    # overridden in the same way..
    #
    # Default comparator: It is used when no special case is applicable.
    #
    comparator(:target => 'R16') {
    }

    ################################################################################################

    # The code below specifies default situations that generate random values
    # for instructions which require arguments to be 16-bit sign-extended values.

    # Generator of 16-bit random values which will be sign-extended to fit the target register.
    #random_word = situation('random', :size => 16, :sign_extend => true)

    ################################################################################################
    random_word = situation('random', :size => 16, :sign_extend => true)

    # Input arguments of all instructions listed below are random words.
    #set_default_situation 'add'   do random_word end

    ################################################################################################

    if i386_assembler == true then
      if is_rev('I80386_GNU') then
        global_label :_start
      else
        text "global _start"
        newline
        label :_start
      end
    else
      text "org 100h ; directive make tiny com file."
    end

    #j :test
    #label :test
  end

  ##################################################################################################
  # Epilogue
  ##################################################################################################

  def post
    if i386_assembler == true then
      label :success
      mov_r16i16 ax, IMM16(1)
      comment 'system call number (sys_exit)'
      int_ IMM16(0x80)
      comment 'call kernel'
    else
      label :success
      mov_r16i16 ax, IMM16(0)
      int_ IMM16(16)
      pseudo 'ret'
    end

    label :error

    section(:name => '.boot') {
      word(0xaa55)
    }
  end

  ##################################################################################################
  # Aliases for GPR Registers
  ##################################################################################################

  ## REG 16

  def ax
    r16(0)
  end

  def cx
    r16(1)
  end

  def dx
    r16(2)
  end

  def bx
    r16(3)
  end

  def sp
    r16(4)
  end

  def bp
    r16(5)
  end

  def si
    r16(6)
  end

  def di
    r16(7)
  end

  def es
    RSEG16(0)
  end

  def cs
    RSEG16(1)
  end

  def ss
    RSEG16(2)
  end

  def ds
    RSEG16(3)
  end

  ##################################################################################################
  # Utility method for printing data stored in memory using labels.
  ##################################################################################################

  def trace_data(begin_label, end_label)
  end

  ###################################################################################################
  # Utility method to remove the specified addressing mode from the list of used registers.
  ###################################################################################################

  def free_register(mode)
    free_allocated_mode mode
  end

end # X86BaseTemplate
