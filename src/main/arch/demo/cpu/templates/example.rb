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

require_relative 'cpu_base'

#
# Description:
#
# The purpose of the Features test template is to demonstrate features of
# MicroTESK. This includes test template facilities to describe instruction
# calls, organize the control flow, set up instruction sequences, generate test
# data for "interesting" situations, add text to the test program, etc. 
#
class ExampleTemplate < CpuBaseTemplate

  #
  # Template settings are overridden here.
  #
  def initialize
    super

    # Sets token for a single-line comment
    @sl_comment_starts_with = ";"

    # Sets starting token for a multi-line comment 
    @ml_comment_starts_with = "/="

    # Sets terminating token for a multi-line comment
    @ml_comment_ends_with = "=/" 
  end

  #
  # The 'run' method contains the main code of the test case. Code that
  # performs initialization and finalization is specified in the 'pre'
  # and 'post' methods correspondingly (see the CpuDemoTemplate class
  # in the 'base_template' file.
  #
  def run
    ############################################################################
    # Text-printing facilities

    # Writes a message to the simulator log
    trace 'Main Section:'
    # Inserts a text to the generated test program
    text 'Main Section:'

    # Printing information on the state of the model (registers, memory)
    # NOTE: trace obtains information on the model state during simulation,
    # while text obtains it during test program generation when simulation
    # is finished (concequently, it works with the most recent state).
    trace 'GPR[0] = %s, M[0] = %s', location('GPR', 0), location('M', 0)
    text  'GPR[0] = %s, M[0] = %s', location('GPR', 0), location('M', 0)

    # Adds an empty line to the test program
    newline
    # Adds a single-line comment to the test program
    comment 'Main Section Starts'

    # Multi-line comments are created in the following way:
    start_comment
    text "Multiline comment. Line 1."
    text "Multiline comment. Line 2."
    text "Multiline comment. Line 3."
    end_comment
    
    ############################################################################
    # Specifyting instruction calls

    # Addressing mode arguments as a hash map
    mov reg(:i => 0), imm(:i => 0xFF)
    mov reg(:i => 1), reg(:i => 0)
    newline

    # Shorter syntax. Addressing mode arguments as a variable-length array
    mov reg(2), imm(0xFF)
    mov reg(3), reg(2)
    newline

    ############################################################################
    # Random instruction arguments

    # Random values vi the 'rand' function.
    # Values can be shared via a variable, for example 'ri'.
    mov reg(ri = rand(0, 15)), imm(5)
    add reg(4), reg(ri)
    newline

    # Random values via a test situation.
    # Values to be generated are specified as '_'. 
    mov reg(ri = _), imm(_) do situation('imm_random', :min => 0, :max => 15) end
    add reg(5), reg(ri)
    newline
    
    ############################################################################
    # Data generation

    # All registers are filled with zeros.
    add reg(1), reg(2) do situation('zero', :size => 8) end

    # Random registers are filled with random values.
    add reg(_), reg(_) do situation('random', :size => 8, :min_imm => 0, :max_imm => 15) end
    newline

    # 'Normal' and 'Overflow' situations for integer addition.
    add reg(3), reg(4) do situation('add', :case => 'normal', :size => 8) end
    add reg(5), reg(6) do situation('add', :case => 'overflow', :size => 8) end
    newline

    # 'Normal' and 'Overflow' situations for integer addition.
    sub reg(7), reg(8) do situation('sub', :case => 'normal', :size => 8) end
    sub reg(9), reg(10) do situation('sub', :case => 'overflow', :size => 8) end
    newline

    ############################################################################
    # How branching works

    # Incrementally descreases value stored in GPR[11] from 5 to 0.
    # Control transfer is performed using labels that are specified
    # in the following way: label :<label_name> 

    mov reg(11), imm(5)
    mov reg(12), imm(1)
    label :start
    trace "GPR[11] = %d", location('GPR', 11)
    jz reg(11), :end
    sub reg(11), reg(12)
    j :start
    label :end
    mov reg(12), imm(0)
    newline

    ############################################################################
    # Building instruction sequences

    # Instruction sequences are described as blocks that use two kind of
    # sequence generators: (1) compositors and (2) combinators.
    # Supported compositors: CATENATION, ROTATION, OVERLAPPING, NESTING, RANDOM
    # Supported combinators: PRODUCT, DIAGONAL, RANDOM    

    # Randomized sequence
    comment 'Randomized sequence'
    block(:compositor => "RANDOM", :combinator => "PRODUCT") {
      block {
        add reg(_), imm(_) do situation('imm_random', :min => 0, :max => 15) end
        sub reg(_), reg(_) do situation('imm_random', :min => 0, :max => 15) end
        mov reg(_), reg(_) do situation('imm_random', :min => 0, :max => 15) end
      }

      block {
        add reg(3), reg(4) do situation('add', :case => 'overflow', :size => 8) end
        sub reg(3), reg(4) do situation('sub', :case => 'overflow', :size => 8) end
        mov reg(4), reg(3)
      }
    }

    newline
    comment 'Main Section Ends'
  end

end
