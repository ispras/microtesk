# Demo of a pre-post wrapper

require_relative "../mtruby"

class DemoPrepost < Template
  def initialize
    super
    @is_executable = no
  end

  def pre
    add pc, pc
    newline
    text "// ^------------------- This was, in fact, a pre-condition"
    newline
  end

  def post
    newline
    text "// v------------------- This is, in fact, a post-condition"
    newline
    mov pc, pc
  end
end