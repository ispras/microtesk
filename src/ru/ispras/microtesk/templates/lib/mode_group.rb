

class ModeGroup

  def initialize
    @modes = Array.new
    @probabilities = Array.new
  end

  def init_with_array(mode_seq, obj)
    mode_seq.each do |i|
      @modes.push obj.method(i)
    end
    @probabilities = nil
    @sum = 0.0
  end

  def init_with_hash(mode_hash, obj)
    mode_hash.keys.each do |i|
      @modes.push obj.method(i)
    end
    @modes.each_with_index do |mode, index|
      if index == 0
        @probabilities[0] = mode_hash[mode]
      else
        @probabilities[index] = @probabilities[index - 1] + mode_hash[mode]
      end
    end

    @sum = @probabilities.reduce(:+)
  end

  def sample (*arguments)
    if @probabilities == nil
      @modes.sample.call(arguments)
    else
      p = Random.new.rand 0.0..sum
      @modes.each_with_index do |mode, index|
        if @probabilities[index] >= p
          mode.call(arguments)
          return
        end
      end
    end
  end

  # TODO: PERMUTATIONS

end