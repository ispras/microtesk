require_relative "../../mtruby"

class PipelineNode
  attr_accessor :name, :paths

  def initialize(name)

    @name = name

    # Graph 'edges' - available paths to other nodes
    # String -> [[Node, Time]] (Instruction name -> where it goes)
    # '*' paths are used
    @paths = Hash.new

  end

  # Subclass this to implement custom transition logic
  # Returns targets and time taken for step to complete
  def goes_to(instruction)
    result = @paths[instruction]
    @paths['*'].each do |ps|
      t = false
      @paths[instruction].each do |p|
        if p.first == ps.first
          t = true
          break
        end
      end
      if !t
        result.push ps
      end
    end

    result
  end

  def add_path_time(instruction, node, time)
    if @paths[instruction] == nil
      @paths[instruction] = Array.new
    end
    @paths[instruction].push [node, time]
  end

  def add_any_path_time(node, time)
    if @paths['*'] == nil
      @paths['*'] = Array.new
    end
    @paths['*'].push [node, time]
  end

  def add_path(instruction, node)
    if @paths[instruction] == nil
      @paths[instruction] = Array.new
    end
    @paths[instruction].push [node, 1]
  end

  def add_any_path(node)
    if @paths['*'] == nil
      @paths['*'] = Array.new
    end
    @paths['*'].push [node, 1]
  end

end

class PipelineDefinition

  attr_accessor :name, :nodes, :root

  def initialize
    @name = ""
    @nodes = Array.new
    @root = nil
  end

  # def add node and stuff


end