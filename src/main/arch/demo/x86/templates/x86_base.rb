#
# MicroTESK X86 Edition
#
# Copyright (c) 2017 Institute for System Programming of the Russian Academy of Sciences
# All Rights Reserved
#
# Institute for System Programming of the Russian Academy of Sciences (ISP RAS)
# 25 Alexander Solzhenitsyn st., Moscow, 109004, Russia
# http://www.ispras.ru
# 

require ENV['TEMPLATE']

class X86BaseTemplate < Template
  def initialize
    super
    # Initialize settings here 
    @setup_memory       = false
    @setup_cache        = false
    @kseg0_cache_policy = 0

    set_option_value 'code-section-keyword', 'section .text'
    set_option_value 'data-section-keyword', 'section .data'

    # Sets the comment token used in test programs
    set_option_value 'comment-token', ';'

    # Sets the indentation token used in test programs
    set_option_value 'indent-token', "\t"

    # Sets the token used in separator lines printed into test programs
    set_option_value 'separator-token', '='

    # set_option_value 'base-virtual-address', 0xa0002000
    # set_option_value 'base-physical-address', 0x00002000
  end

  ##################################################################################################
  # Prologue
  ##################################################################################################

  def pre
    ################################################################################################

    #
    # Information on data types to be used in data sections.
    #
    data_config(:text => 'section .data', :target => 'MEM', :base_virtual_address => 0x4000) {
      define_type :id => :byte,  :text => 'db',  :type => type('card', 8)
      define_type :id => :word,  :text => 'dw',  :type => type('card', 16)

      define_space        :id => :space,  :text => '.space',  :fill_with => 0
      define_ascii_string :id => :ascii,  :text => '.ascii',  :zero_term => false
      define_ascii_string :id => :asciiz, :text => '.asciiz', :zero_term => true
    }

    #
    # Simple exception handler. Continues execution from the next instruction.
    #
    exception_handler {
      # TODO
    }

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

    text "global _start"
    newline

    label :_start
    #j :test
    #label :test
  end

  ##################################################################################################
  # Epilogue
  ##################################################################################################


  ##################################################################################################
  # Epilogue
  ##################################################################################################

  def post
    label :success
    mov_r16i16 ax, IMM16(1)
    text ";system call number (sys_exit)"
    int_ IMM16(0x80)
    text ";call kernel"

    label :error
    newline
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
  # Shortcut methods to access memory resources in debug messages.
  ##################################################################################################

  def gpr_observer(index)
    location('GPR', index)
  end

  ##################################################################################################
  # Utility method for printing data stored in memory using labels.
  ##################################################################################################

  def trace_data(begin_label, end_label)
  end

  ##################################################################################################
  # Utility method to specify a random register that is not used in the current test case.
  ##################################################################################################

  def get_register(attrs = {})
    if nil == @free_register_allocator
      @free_register_allocator = mode_allocator('FREE')
    end

    r16(_ @free_register_allocator, attrs)
  end

  ###################################################################################################
  # Utility method to remove the specified addressing mode from the list of used registers.
  ###################################################################################################

  def free_register(mode)
    free_allocated_mode mode
  end

end # X86BaseTemplate
