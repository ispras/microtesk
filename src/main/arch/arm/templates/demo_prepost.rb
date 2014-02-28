# Demo of a pre-post wrapper

require_relative "../../../libs/ruby/templates/mtruby"

class DemoPrepost < Template
  def initialize
    super
    @is_executable = no
  end

  def pre
#   super.pre
#   add mem("i" => 12), mem("i" => 13)
#   newline
#   text "// This line is technically a pre-condition"
#   newline
  end

  def post
#   newline
#   text "// This line is technically a post-condition"
#   newline
#   add mem("i" => 23), 23
#   super.post
  end
end
