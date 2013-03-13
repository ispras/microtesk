# Demo template time!

require_relative "../mtruby"
require_relative "./demo_prepost"

class DemoTemplate < DemoPrepost
  def initialize
    super
    @is_executable = yes
  end

  def run

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

    newline
    text "// Basic instructions"
    newline

    add mem(:i => 1), mem(:i => 1)
    sub mem(:i => 2), imm(4)

    newline
    text "// Atomic block"
    newline

    atomic {
      mov mem(25), mem(26)
      add mem(27), imm(28)
      sub mem(29), imm(30)
    }

    newline
    text "// Block group sampling"
    newline

    block_group "my_group" do
          prob 0.2
          mov mem(30), mem(31)
          prob 0.2
          add mem(32), imm(41)
          prob 0.6
          sub mem(33), imm(42)
    end

    15.times do
      my_group.sample
    end

    newline
    text "// Block group - entire"
    newline

    my_group.all


  end
end