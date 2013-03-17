class ExecDebug
  attr_accessor :exec_debug_block
  def initialize(&block)
    @exec_debug_block = block
  end
end