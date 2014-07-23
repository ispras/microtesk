#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# demo_template.rb, May 20, 2014 3:06:13 PM
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

require_relative 'demo_prepost'

#
# Description:
#
# The purpose of the DemoTemplate test template is to demonstrate feaures of
# MicroTESK. This includes test template facilities to specify instruction
# calls and addression modes, to organize the control flow, to set up 
# instruction sequences, to add text to the test program, etc. 
#
class DemoTemplate < DemoPrepost

  def initialize
    super
    @is_executable = true

    @sl_comment_starts_with = ";" 
    @ml_comment_starts_with = "/="
    @ml_comment_ends_with   = "=/" 
  end

  def run
    trace 'Main Section:'
    comment 'Main Section Starts'
    newline
    
    start_comment
    text "Multiline comment. Line 1."
    text "Multiline comment. Line 2."
    text "Multiline comment. Line 3."
    end_comment

    # Addressing mode arguments as a hash map
    mov reg(:i => 0), imm(:i => 0xFF)
    mov reg(:i => 1), reg(:i => 0)
    newline

    # Storter syntax. Addressing mode arguments as a variable-length array
    mov reg(2), imm(0xFF)
    mov reg(3), reg(2)
    newline

    #add m[10], r[15]                                     # Indexing register arrays
    #sub pc, pc do overflow(:op1 => 123, :op2 => 456) end # Situations with params
    #mov ({:name => :value}), m, pc[0]                    # Instruction attribute
    #newline                                              # Newline
    #text "// This is a really, really useless test"      # Custom/comment text
    #newline
    #sub m[10..20], m[k=rand(0...16)]                     # Ranges as array index
    #newline
    #3.times do
    #  sub m, r
    #  mov r[1], r[k] do normal end                       # Situations w/o params
    #  newline
    #end
    #newline
    #(1..5).each do |i|
    #  mov m[i], m[i+1]                                   # Ruby being smart
    #end

    # newline
    # text "// Basic instructions"
    # newline

    add mem(:i => 1), mem(:i => 1)
    sub mem(:i => 2), imm(4)

    # newline
    # text "// Atomic block"
    # newline

    # atomic {
      mov mem(25), mem(26)
      add mem(27), imm(28)
      sub mem(29), imm(30)
    # }

    # newline
    # text "// Block group sampling"
    # newline

#    block_group "my_group" do
#          prob 0.2
#          mov mem(30), mem(31)
#          prob 0.2
#          add mem(32), imm(41)
#          prob 0.6
#          sub mem(33), imm(42)
#    end
#
#    15.times do
#      my_group.sample
#    end
#
#    newline
#    text "// Block group - entire"
#    newline
#
#    my_group.all
      
    comment 'Main Section Ends'
    newline
  end

end
