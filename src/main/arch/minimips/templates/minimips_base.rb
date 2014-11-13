require ENV['TEMPLATE']

class MinimipsBaseTemplate < Template

  def initialize
    super
    # Initialize settings here 
  end

  def pre
    # Place your initialization code here
  end

  def post
    # Place your finalization code here
  end

  def gpr(index)
    location('REGISTERS', index)
  end
end