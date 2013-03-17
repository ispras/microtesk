
# This class represents a block of instructions with attached test situation. This is the base abstraction that the
# templates work with.

class InstructionBlock
  attr_accessor :instructions, :situations, :j_caller

  # @instructions: Array of Instruction
  # TODO > @situations: Array of Situation

  def initialize (template)
    @instructions = Array.new
    @situations = Array.new
    @code = Array.new
    @j_caller = nil
    @j_monitor = template.j_monitor
    @template = self

    # Labels are maintained by the template system, TODO: doesn't really work yet
    #@labels = Hash.new
    #@r_labels = Hash.new
  end

  def outlog
    #@instructions.each {|i| i.output }
    @code.each {|c| puts c
                #puts '--- newline debug ---'
    }
  end

  def output(file)
    #@instructions.each {|i| i.output(file)}
    @code.each {|c| file.puts c}
  end

  def j_build(j_simulator, template)
    j_block_builder = j_simulator.createCallBlock()
    @instructions.each do |i|
      i.j_build(j_block_builder.addCall(i.name), template)
    end

    # TODO > situations

    @j_caller = j_block_builder.getCallBlock()

  end

  def j_call(r_labels, should_execute)
    if j_caller == nil
      puts "MTRuby: Trying to call an uninitialized instruction block"
      return
    end

    @code = Array.new

    # TODO: goto label code goes HERE!!!!!!!!!
    # Logic: reset PC before every execute
    #        execute
    #        check if PC corresponds to any label - return the label to the template!!!!!!!
    # no labels inside block because no 1-1 correspondence between build and call

    jumped = false
    pc = 0

    j_call = nil

    (0 .. j_caller.getCount() - 1).each do |i|

      if(!jumped && should_execute)
        @j_monitor.setPC(0)
      end

      j_call = j_caller.getCall(i)

      if(!jumped && should_execute)
        j_call.execute()
      end

      text = j_call.getText()
      @code.push(text)
      if(should_execute)
        puts "Running " + text
      end

      if(should_execute)
        pc = @j_monitor.getPC()
        if(r_labels.keys.include?(pc))
          jumped = true
        end
      end
    end
  jumped ? pc : nil

  rescue Exception => ex
    if j_call != nil
      text = j_call.getText()
      puts "Failed on " + text
    end
    raise ex
  end

  def receive(instruction)
    @instructions.push(instruction)
  end

  #def receive_label(name, id)
  #  @labels[name] = [@instructions.count, ll]
  #  @r_labels[id] = [name, @instructions.count]
  #end

end