# Demo template time!

require_relative "../mtruby"
require_relative "./demo_prepost"

class DemoTemplate < DemoPrepost
  def initialize
    super
    @is_executable = yes
  end

  def run

    add m[10], r[15]                                     # Indexing register arrays
    sub pc, pc do overflow(:op1 => 123, :op2 => 456) end # Situations with params
    mov ({:name => :value}), m, pc[0]                    # Instruction attribute
    newline                                              # Newline
    text "// This is a really, really useless test"      # Custom/comment text
    newline
    sub m[10..20], m[k=rand(0...16)]                     # Ranges as array index
    newline
    3.times do
      sub m, r
      mov r[1], r[k] do normal end                       # Situations w/o params
      newline
    end
    newline
    (1..5).each do |i|
      mov m[i], m[i+1]                                   # Ruby being smart
    end

  end
end