class NoValue

  attr_accessor :aug_value, :is_immediate

  def initialize(aug_value = nil)

    @aug_value = aug_value
    @is_immediate = false

  end

end