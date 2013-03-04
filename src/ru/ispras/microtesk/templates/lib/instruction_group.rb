

class InstructionGroup

  def initialize
    @instructions = Array.new
    @probabilities = Array.new
  end

  def init_with_array(inst_seq, obj)
    inst_seq.each do |i|
      @instructions.push obj.method(i)
    end
    @probabilities = nil
    @sum = 0.0
  end

  def init_with_hash(inst_hash, obj)
    inst_hash.keys.each do |i|
      @instructions.push obj.method(i)
    end
    @instructions.each_with_index do |inst, index|
      if index == 0
        @probabilities[0] = inst_hash[inst]
      else
        @probabilities[index] = @probabilities[index - 1] + inst_hash[inst]
      end
    end

    @sum = @probabilities.reduce(:+)
  end

  def sample (*arguments, &situations)
    if @probabilities == nil
      @instructions.sample.call(arguments, &situations)
    else
      p = Random.new.rand 0.0..sum
      @instructions.each_with_index do |inst, index|
        if @probabilities[index] >= p
          inst.call(arguments, &situations)
          return
        end
      end
    end
  end

  def all (*arguments, &situations)
    @instructions.each do |i|
      i.call(*arguments, &situations)
    end
  end

  # TODO: PERMUTATIONS

end