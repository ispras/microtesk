# Demo of a pre-post wrapper

require_relative "../mtruby"

class DemoPrepost < Template
  def initialize
    super
    @is_executable = no
  end

  def pre
#    super.pre
    add mem("i" => 12), mem("i" => 13)
    newline
    text "// ^------------------- This was, in fact, a pre-condition"
    newline
  end

  def post
    newline
    text "// v------------------- This is, in fact, a post-condition"
    newline
    add mem("i" => 23), 23
 #   super.post
  end
end