
# Mixins for the Template class
require_relative "output"
require_relative "state_observer"

# Other dependencies
require_relative "config"
require_relative "constructs"
require_relative "engine"
require_relative "utils"

#
# Description: 
#
# The Settings module describes settings used in test templates and
# provides default values for these settings. It is includes in the
# Template class as a mixin. The settings can be overridden for
# specific test templates. To do this, instance variables must be
# assigned new values in the initialize method of the corresponding
# test template class. 
#
module Settings

  # Specifies whether the template is a concrete template used as
  # a basis for test generation or it is an abstract template designed
  # to be reused by other test templates that inherit from it. In the
  # latter case, no tests are generated.
  # TODO: This feature needs to be reviewed.
  attr_reader :is_executable

  # Print the generated code to the console.
  attr_reader :use_stdout

  # Print instructions being simulated to the console.   
  attr_reader :log_execution

  # Text that starts single-line comments.
  attr_reader :sl_comment_starts_with

  # Text that starts multi-line comments.
  attr_reader :ml_comment_starts_with

  # Text that terminates multi-line comments.
  attr_reader :ml_comment_ends_with

  #
  # Assigns default values to the attributes.
  # 
  def initialize
    @is_executable = true
    @use_stdout    = true
    @log_execution = true

    @sl_comment_starts_with = "// "
    @ml_comment_starts_with = "/*"
    @ml_comment_ends_with   = "*/"
  end

end

class Template
  include Settings
  include StateObserver
  include Output  

  @@template_classes = Array.new

  def initialize
    super
   
    # Important variables for core Template functionality
    @core_block = InstructionBlock.new

    @instruction_receiver = @core_block
    @receiver_stack = [@core_block]

    @final_sequences = Array.new
  end

  def self.template_classes
    @@template_classes
  end

  def self.set_model(j_model)
    Engine.model = j_model
    StateObserver.model = j_model
  end

  # This method adds every subclass of Template to the list of templates to parse
  def self.inherited(subclass)
    @@template_classes.push subclass
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

  def add_output(o)
    @instruction_receiver.receive o
  end  

  # --- Special "no value" method ---

  def _(aug_value = nil)
    NoValue.new(aug_value)
  end

  def __(aug_value = nil)
    v = NoValue.new(aug_value)
    v.is_immediate = true
    v
  end

  # -------------------------------------------------- #
  # Execution                                          #
  # -------------------------------------------------- #
  
  def get_block_iterator
    bl = @core_block.build(Engine.j_bbf)
    bl.getIterator()
  end

  def execute
    puts
    puts "---------- Start build ----------"
    puts

    bl_iter = get_block_iterator

    puts
    puts "---------- Start execute ----------"
    puts

    executor = Executor.new self, bl_iter, log_execution
    executor.execute
    @final_sequences = executor.get_concrete_calls
  end

  # Print out the executable program
  def output(filename)
    puts
    puts "---------- Start output ----------"
    puts

    printer = Printer.new(filename, self, self)
    @final_sequences.each do |fs|
      printer.print_sequence fs
    end
  end

end

class Executor

  def initialize(context, abstract_calls, is_log)
    @context = context
    @abstract_calls = abstract_calls
    @log_execution = is_log
  end
  
  def get_concrete_calls
    @final_sequences
  end

  def execute

    bl_iter = @abstract_calls
    
    # Preprocess labels
    @labels = Hash.new

    # look for labels in the sequences
    bl_iter.init()
    sn = 0
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
        @final_sequences[i] = Engine.generate_data sequences[i]
      end
    end

    
    
  end
  
  
def exec_sequence(seq, gen, id, label)
    r_gen = gen
    if gen == nil
      # TODO NEED EXCEPTION HANDLER
      r_gen = Engine.generate_data seq
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
      
      exec = inst.getExecutable()

      print_debug inst.getAttribute("b_runtime")

      if @log_execution
        puts exec.getText()
      end

      exec.execute()
      # execute some debug code too

      print_debug inst.getAttribute("f_runtime")

      # Labels
      jump = StateObserver.control_transfer_status

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

  def print_debug(debug_arr)
    if debug_arr.is_a? Array
      debug_arr.each do |debug|
        text = debug.evaluate_to_text(@context) 
        if nil != text
          puts text
        end
      end
    end
  end 

end

class Printer

  def initialize(filename, settings, context)
    raise "Wrong setting type." unless settings.is_a? Settings
    raise "Wrong context type." unless context.is_a? StateObserver

    @filename  = filename
    @settings  = settings
    @context   = context
    @file      = nil
    @is_header = true
  end

  def print_sequence(sequence)
    print_header
    sequence.each do |inst|
      print_outputs inst.getAttribute("b_output")
      print_labels  inst.getAttribute("b_labels")
      print_text    inst.getExecutable().getText()
      print_labels  inst.getAttribute("f_labels")
      print_outputs inst.getAttribute("f_output")
    end
  end

private

  def use_file?
    @filename != nil and @filename != ""
  end

  def generate_header
    slcs = @settings.sl_comment_starts_with
    HEADER_TEXT % [slcs, slcs, Time.new, slcs, slcs, slcs, slcs]
  end

  def print_to_stdout(text)
    if @settings.use_stdout
      puts text
    end
  end

  def print_to_file(text)
    if use_file?
      if @file == nil
        @file = File.open(@filename, 'w')
      end
      @file.puts text
    end
  end

  def print_text(text)
    print_to_stdout text
    print_to_file text
  end

  def print_header
    if @is_header
      print_text generate_header
      @is_header = false
    end
  end

  def print_outputs(arr)
    return unless arr.is_a? Array
    arr.each do |item|
      s = item.evaluate_to_text(self)
      print_text s if s != nil
    end
  end

  def print_labels(arr)
    return unless arr.is_a? Array
    arr.each do |label|
      text = label.first
      label[1].each { |t| text += "_" + t.to_s }
      print_text text + ":"
    end
  end

end
