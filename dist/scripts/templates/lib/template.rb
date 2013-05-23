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
    te = TestEngine.getInstance(j_model)
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
    puts
    puts " ---------- Start build ----------"
    puts
    
    bl = @core_block.build(@j_bbf)
    bl_iter = bl.getIterator()

    puts
    puts " ---------- Start execute ----------"
    puts
    
    # Preprocess labels
    @labels = Hash.new

    # look for labels in the sequences
    bl_iter.init()
    sn = 0;
    sequences = Array.new
    
    while bl_iter.hasValue()
      seq = bl_iter.value()

      seq.each_with_index do |inst, i|
        #TODO check if sequences have nulls?
        if inst == nil
          next
        end
        
        f_labels = inst.getAttribute("f_labels")
        b_labels = inst.getAttribute("b_labels")

        #process labels
      
        if f_labels.is_a? Array
          f_labels.each do |label|
            @labels[label] = [sn, i + 1]
          end
        end
        
        if b_labels.is_a? Array
          b_labels.each do |label|
            @labels[label] = [sn, i]
          end 
        end        
      end
      sn += 1
      sequences.push seq
      
      bl_iter.next()
    end

    # Execute and generate data in the process
    generated = Array.new
    @final_sequences = Array.new(sequences.length)
    @final_sequences.each_with_index do |sq, i| 
      @final_sequences[i] = nil 
    end
    
    cur_seq = 0
    continue = true
    label = nil
    
    # puts @labels.to_s
    
    # execution loop
    while continue && cur_seq < sequences.length
      fin, label = exec_sequence(sequences[cur_seq], @final_sequences[cur_seq], cur_seq, label)
      
      if @final_sequences[cur_seq] == nil && cur_seq < sequences.length
        @final_sequences[cur_seq] = fin
      end
      
      if label == nil
        goto = cur_seq + 1
      else
        unstack = [] + label[1]
        while @labels[[label.first, unstack]] == nil && unstack.length > 0
          unstack.pop
        end
        
        result = @labels[[label.first, unstack]]
        
        if result == nil
          goto = cur_seq + 1
          text = label.first
          label[1].each do |t|
            text += "_" + t.to_s
          end
          puts "Label " + label.first + " doesn't exist"
        else
          label = [label.first, unstack]
          goto = result.first
        end
      end      
      
      
      if (goto >= sn + 1) or (goto == -1 && cur_seq >= sn)
        continue = false
      else
        cur_seq = goto
      end
      
    end
      
    # Generate the remaining sequences  
    @final_sequences.each_with_index do |s, i|
      if s == nil && i < sequences.length
#        if sequences[i] == nil
#          puts "what the fuck " + i.to_s
#        end
        @final_sequences[i] = @j_dg.generate(sequences[i])
      end
    end

  end
  
  def exec_sequence(seq, gen, id, label)
    r_gen = gen
    if gen == nil
      r_gen = @j_dg.generate(seq)
    end
    
    labels = Hash.new
    
    r_gen.each_with_index do |inst, i|
      f_labels = inst.getAttribute("f_labels")
      b_labels = inst.getAttribute("b_labels")
      
      #process labels
      
      if f_labels.is_a? Array
        f_labels.each do |f_label|
          labels[f_label] = i + 1
          # puts "Registered f_label " + f_label
        end
      end
      
      if b_labels.is_a? Array
        b_labels.each do |b_label|
          labels[b_label] = i
          # puts "Registered b_label " + b_label
        end        
      end
    end
    
    cur_inst = 0
    
    if label != nil
      cur_inst = labels[label]
      # puts label.to_s
      # puts labels.to_s
    end
    
    total_inst = r_gen.length
    
    continue = true
    
    jump_target = nil
    
    while continue && cur_inst < total_inst
      
      inst = r_gen[cur_inst]
      i_labels = inst.getAttribute("labels")
      
      f_debug = inst.getAttribute("f_runtime_debug")
      b_debug = inst.getAttribute("b_runtime_debug")
      
      if f_debug.is_a? Array
        f_debug.each do |f_d|
          self.instance_exec &f_d
        end
      end
      
      exec = inst.getExecutable()

      if @log_execution
        puts exec.getText()
      end

      if b_debug.is_a? Array
        b_debug.each do |b_d|
          self.instance_exec &b_d
        end
      end
            
      exec.execute()
      # execute some debug code too
      
      # Labels
      jump = @j_monitor.getControlTransferStatus()
      
      # TODO: Support instructions with 2+ labels (needs API)
      
      if jump > 0
        target = inst.getAttribute("labels").first.first
        if target == nil || target.first == nil
          puts "Jump to nil label, transfer status: " + jump.to_s
        elsif labels.has_key? target
          cur_inst = labels[target]
          if @log_execution
            text = target.first
            target[1].each do |t|
              text += "_" + t.to_s
            end
            puts "Jump (internal) to label: " + text
          end
          next
        else
          jump_target = target
          if @log_execution
            text = target.first
            target[1].each do |t|
              text += "_" + t.to_s
            end
            puts "Jump (external) to label: " + text

          end
          break
        end
      end
      
      # If there weren't any jumps, continue on to the next instruction
      cur_inst += 1
    end
    
    [r_gen, jump_target]
    
  end

  # Print out the executable program
  def output(filename)
    
    puts
    puts " ---------- Start output ----------"
    puts
    
    use_file = true
    if filename == nil or filename == ""
      use_file = false
    else
      file = File.open(filename, 'w')
    end
      
      @final_sequences.each do |fs|
        fs.each do |inst|
          
          f_debug = inst.getAttribute("f_output_debug")
          b_debug = inst.getAttribute("b_output_debug")
      
          f_string = inst.getAttribute("f_output_string")
          b_string = inst.getAttribute("b_output_string")
          
          f_labels = inst.getAttribute("f_labels")
          b_labels = inst.getAttribute("b_labels")
      
          #process labels
          
          if b_debug.is_a? Array
            b_debug.each do |b_d|
              s = self.instance_eval &b_d
              if use_file
                file.puts s
              end
              if @use_stdout
                puts s
              end
            end
          end
          
          if b_string.is_a? Array
            b_string.each do |b_s|
              if use_file
                file.puts s
              end
              if @use_stdout
                puts b_s
              end
            end
          end
          
          if b_labels.is_a? Array
            b_labels.each do |b_label|
              
              text = b_label.first
              b_label[1].each do |t|
                text += "_" + t.to_s
              end
              
              if use_file
                file.puts text + ":"
              end
              if @use_stdout
                puts text + ":"
              end
            end
          end

          
          if use_file
            file.puts inst.getExecutable().getText()
          end
          if @use_stdout
            puts inst.getExecutable().getText()
          end
          
          if f_labels.is_a? Array
            f_labels.each do |f_label|
              
              text = f_label.first
              f_label[1].each do |t|
                text += "_" + t.to_s
              end
              
              if use_file
                file.puts text + ":"
              end
              if @use_stdout
                puts text + ":"
              end
            end
          end
          
          if f_debug.is_a? Array
            f_debug.each do |f_d|
              s = self.instance_eval &f_d
              if use_file
                file.puts s
              end
              if @use_stdout
                puts s
              end
            end
          end
          
          if f_string.is_a? Array
            f_string.each do |f_s|
              if use_file
                file.puts s
              end
              if @use_stdout
                puts f_s
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