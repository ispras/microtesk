class ExecOutput
  attr_accessor :exec_output_block

  def initialize(&block)
    @exec_output_block = block
    @output = ""
  end

  def yield_output
    @output = @exec_output_block.yield
  end

  def outlog
    puts @output
  end

  def output(file)
    file.puts @output
  end

end