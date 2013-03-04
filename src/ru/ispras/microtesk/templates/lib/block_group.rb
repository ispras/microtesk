

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
    @probabilities.push(p)
    @sum += p
  end

  def any
    if @probabilities == nil
      return @instructions.sample
    else
      p = Random.new.rand 0.0..sum
      @instructions.each_with_index do |inst, index|
        if @probabilities[index] >= p
          @instruction_receiver.receive(inst)
          return
        end
      end
    end
  end

  def all
    @instructions.each do |i|
      @instruction_receiver.receive(i)
    end
  end

  # TODO: PERMUTATIONS

end