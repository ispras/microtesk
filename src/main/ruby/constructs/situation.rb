class Situation

  attr_accessor :probabilities, :targets, :op, :name

  def initialize(name = "NoSituation")
    @op = :none # :and, :or; :none means tree leaf

    @probabilities = Array.new
    @targets = Array.new

    @name = name
  end

  def & (situation)
    root = Situation.new
    root.op = :and
    root.targets.push self
    root.targets.push situation
    root.probabilities = [0.5, 0.5]
    root
  end

  def | (situation)
    root = Situation.new
    root.op = :or
    root.targets.push self
    root.targets.push situation
    root.probabilities = [0.5, 0.5]
    root
  end

  def sample
    sum = rand * @probabilities.reduce(:+)
    @probabilities.each_with_index do |p, i|
      if sum <= p
        return @targets[i]
      else
        sum -= p
      end
    end

    puts "Situation#sample probability glitch"

    @targets.last

  end

end