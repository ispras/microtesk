

class BlockGroup

  def initialize (instruction_receiver)
    @instructions = Array.new
    @probabilities = Array.new
    @sum = 0
    @instruction_receiver = instruction_receiver
  end

  def receive(instruction)
    @instructions.push(instruction)
  end

  def receive_probability(p)
    @sum += p
    @probabilities.push(@sum)
  end

  def sample
    if @probabilities == nil || @probabilities.count == 0
      @instruction_receiver.receive @instructions.sample
    else
      p = Random.new.rand 0.0..@sum
      @instructions.each_with_index do |inst, index|
        if @probabilities[index] >= p
          @instruction_receiver.receive(inst)
          return
        end
      end
      @instruction_receiver.receive(@instructions.last)
    end
  end

  def all
    @instructions.each do |i|
      @instruction_receiver.receive(i)
    end
  end

  # TODO: PERMUTATIONS

end