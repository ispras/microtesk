require_relative "instruction_block"
require_relative "mode_group"
require_relative "block_group"
require_relative "instruction_group"
require_relative "output_debug"
require_relative "output_string"
require_relative "runtime_debug"
require_relative "label"

class Template

  attr_accessor :is_executable, :j_model, :j_monitor, :j_bbf, :j_dg

  def initialize
    super

    # User settings
    @is_executable = true
    @use_stdout = true
    @log_execution = true

    # Important variables for core Template functionality
    @core_block = InstructionBlock.new

    @instruction_receiver = @core_block
    @receiver_stack = [@core_block]

    @final_sequences = Array.new

    # V2 LEFTOVERS
    @probability_receiver = nil
  end

  def set_model  (j_model)
    @j_model = j_model
    @j_monitor = @j_model.getStateObserver()
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    te = TestEngine.new(j_model)
    @j_bbf = te.getBlockBuilders()
    @j_dg = te.getDataGenerator()
  end

  # This method adds every subclass of Template to the list of templates to parse
  def self.inherited(subclass)
    $template_classes.push subclass
  end

  # Hack to allow limited use of capslocked characters
  def method_missing(meth, *args, &block)
    if self.respond_to?(meth.to_s.downcase)
      self.send meth.to_s.downcase.to_sym, *args, &block
    else
      super
    end
  end

  # -------------------------------------------------- #
  # Main template writing methods                      #
  # -------------------------------------------------- #

  # Pre-condition instructions template
  def pre

  end

  # Main instructions template
  def run
    puts "MTRuby: warning: Trying to execute the original Template#run."
  end

  # Post-condition instructions template
  def post

  end

  # Run... pre, run and post! This method parses the template
  def parse
    pre
    run
    post
  end

  def block(attributes = {}, &contents)
    b = InstructionBlock.new
    b.attributes = attributes

    @receiver_stack.push b
    @instruction_receiver = @receiver_stack.last

    self.instance_eval &contents

    @receiver_stack.pop
    @instruction_receiver = @receiver_stack.last

    @instruction_receiver.receive b
  end

  def label(name)
    l = Label.new
    l.name = name.to_s
    @instruction_receiver.receive l
  end
  
  def text(text)
    t = OutputDebug.new
    t.text = text
    @instruction_receiver.receive t
  end
  
  def debug(&block)
    d = RuntimeDebug.new
    d.proc = block
    @instruction_receiver.receive d
  end
  
  def out_debug(&block)
    o = OutputDebug.new
    o.proc = block
    @instruction_receiver.receive o
  end

  # -------------------------------------------------- #
  # Memory-related methods                             #
  # -------------------------------------------------- #

  def get_loc_value (string, index = nil)
    if index == nil
      @j_monitor.accessLocation(string).getValue()
    else
      @j_monitor.accessLocation(string, index).getValue()
    end
  end

  def get_loc_size (string, index = nil)
    if index == nil
      @j_monitor.accessLocation(string).getBitSize()
    else
      @j_monitor.accessLocation(string, index).getBitSize()
    end
  end

  def set_loc_value (value, string, index = nil)
    if index == nil
      @j_monitor.accessLocation(string).setValue(value)
    else
      @j_monitor.accessLocation(string, index).setValue(value)
    end
  end


  # -------------------------------------------------- #
  # Execution                                          #
  # -------------------------------------------------- #

  # TODO: everything

  def execute
    bl = @core_block.build(@j_bbf)

    # Preprocess labels
    @labels = Hash.new

    # look for labels in the sequences
    bl.init()
    sn = 0;
    sequences = Array.new
    
    while bl.hasValue()
      seq = bl.value()

      seq.each_with_index do |inst, i|
        f_labels = inst.getAttribute("f_labels")
        b_labels = inst.getAttribute("b_labels")

        #process labels
        
        f_labels.each do |label|
          @labels[label] = [sn, i + 1]
        end
        
        b_labels.each do |label|
          @labels[label] = [sn, i]
        end        
      end
      sn += 1
      sequences.push seq
    end

    # Execute and generate data in the process
    generated = Array.new
    @final_sequences = Array.new(sn + 1)
    
    cur_seq = 0
    continue = true
    label = nil
    
    # execution loop
    while continue
      fin, label = exec_sequence(sequences[cur_seq], @final_sequences[cur_seq], cur_seq, label)
      
      goto = @labels[label].first
      
      if @final_sequences[sn] == nil
        @final_sequences[sn] = fin
      end
      
      if label == nil
        goto = cur_seq + 1
      end      
      
      if (goto >= sn + 1) or (goto == -1 && cur_seq >= sn)
        continue = false
      else
        cur_seq = goto
      end
    end
      
    # Generate the remaining sequences  
    @final_sequences.each_with_index do |s, i|
      if s == nil
        @final_sequences[i] = @j_dg.generate(sequences[i])
      end
    end

  end
  
  def exec_sequence(seq, gen, id, label)
    r_gen = gen
    if gen == null
      r_gen = @j_dg.generate(seq)
    end
    
    labels = Hash.new
    
    r_gen.each_with_index do |inst, i|
      f_labels = inst.getAttribute("f_labels")
      b_labels = inst.getAttribute("b_labels")
      
      #process labels
    
      f_labels.each do |f_label|
        labels[f_label] = i + 1
      end
    
      b_labels.each do |b_label|
        labels[b_label] = i
      end        
    end
    
    cur_inst = 0
    
    if label != nil
      cur_inst = labels[label]
    end
    
    total_inst = r_gen.length
    
    continue = true
    
    jump_target = nil
    
    while continue && cur_inst < total_inst
      
      inst = r_gen[cur_inst]
      i_labels = inst.getAttribute("labels")
      
      f_debug = inst.getAttribute("f_runtime_debug")
      b_debug = inst.getAttribute("b_runtime_debug")
      
      f_debug.each do |f_d|
        self.instance_exec &f_d
      end
      
      exec = inst.getExecutable()

      if @log_execution
        puts exec.getText()
      end

      b_debug.each do |b_d|
        self.instance_exec &b_d
      end
            
      exec.execute()
      # execute some debug code too
      
      # LET'S SUPPOSE WE GET SOME LABELS HERE
      # something = inst.didWeJumpToSomeLabelOrWhat?()
      # TODO: IInstructionCall _does not_ have labels?..
      
      # if there was a jump
      # if labels.has_key? target
      #   cur_inst = labels[target]
      #   next
      # else
      #   jump_target = target      
      #   break
      # end
      
      cur_inst += 1
    end
    
    
  end

  # Print out the executable program
  def output(filename)
    File.open(filename) do |file|
      
      @final_sequences.each do |fs|
        fs.each do |inst|
          
          f_debug = inst.getAttribute("f_output_debug")
          b_debug = inst.getAttribute("b_output_debug")
      
          f_string = inst.getAttribute("f_output_string")
          b_string = inst.getAttribute("b_output_string")
          
          f_debug.each do |f_d|
            s = self.instance_eval &f_d
            file.puts s
            if @use_stdout
              puts s
            end
          end
          
          f_string.each do |f_s|
            file.puts f_s
            if @use_stdout
              puts f_s
            end
          end
          
          file.puts inst.getExecutable().getText()
          if @use_stdout
            puts inst.getExecutable().getText()
          end
          
          b_debug.each do |b_d|
            s = self.instance_eval &b_d
            file.puts s
            if @use_stdout
              puts s
            end
          end
          
          b_string.each do |b_s|
            file.puts b_s
            if @use_stdout
              puts b_s
            end
          end
          
        end
      end
      
    end
  end

  # -------------------------------------------------- #
  # Group-related methods                              #
  # -------------------------------------------------- #

  # VERY UNTESTED leftovers from the previous version ("V2", this is V3)
  # Should work with the applied fixes but I'd be very careful to use these

  # As things stand this is just a little discrete probability utility that
  # may or may not find its way into the potential ruby part of the test engine

  def group(name, instructions)
      i = InstructionGroup.new
      if instructions.is_a?(Array)
        i.init_with_array(instructions, self)
      elsif instructions.is_a?(Hash)
        i.init_with_hash(instructions, self)
      else
        raise "MTRuby: group must accept either an array of instructions or an instruction => probability Hash"
      end

      p = lambda do
        return i
      end

      method_name = name
      while Template.respond_to?(method_name)
        method_name = "gr_" + method_name
      end
      Template.send(:define_method, method_name, p)

  end

  def mode_group(name, modes)
      i = ModeGroup.new
      if modes.is_a?(Array)
        i.init_with_array(modes, self)
      elsif modes.is_a?(Hash)
        i.init_with_hash(modes, self)
      else
        raise "MTRuby: mode group must accept either an array of modes or a mode => probability Hash"
      end

      p = lambda do
        return i
      end

      method_name = name
      while Template.respond_to?(method_name)
        method_name = "mgr_" + method_name
      end
      Template.send(:define_method, method_name, p)

  end

  def block_group(name, &block)
    bl = BlockGroup.new(self)
    temp = @instruction_receiver
    p_temp = @probability_receiver

    @instruction_receiver = bl
    @probability_receiver = bl
    if block != nil
      block.yield
    end
    @instruction_receiver = temp
    @probability_receiver = p_temp

    p = lambda do
      return bl
    end

    method_name = name
    while Template.respond_to?(method_name)
      method_name = "bg_" + method_name
    end
    Template.send(:define_method, method_name, p)
  end

  def prob(p)
    if @probability_receiver == nil
      puts "MTRuby: warning: probabilities can only be set inside a block group"
    end
    @probability_receiver.receive_probability(p)
  end


end