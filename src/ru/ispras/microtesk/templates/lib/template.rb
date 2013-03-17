#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Template class        #
#                              #


require 'fileutils'
require_relative 'instruction_group'
require_relative 'mode_group'
require_relative 'block_group'

class Template
  attr_accessor :is_executable, :j_model, :j_monitor

  def initialize
    super

    # Until I find a way to properly set every subclass's
    # is_executable to true by default and this class's to
    # false by default...
    @is_executable = true

    # instruction blocks, labels, other stuff
    @items = Array.new

    @instruction_receiver = self
    @probability_receiver = nil

    # Labels are maintained by the template system, TODO: doesn't really work yet
    @labels = {:start => [0, 1000]}
    @r_labels = {1000 => [:start, 0]}
    @last_label = 1000

  end

  def set_model  (j_model)
    @j_model = j_model
    @j_monitor = @j_model.getModelStateMonitor()
  end

  # This method adds every subclass of Template to the list of templates to parse
  def self.inherited(subclass)
    $template_classes.push subclass
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

  # -------------------------------------------------- #
  # Additional template writing methods                #
  # -------------------------------------------------- #

  def text(raw_text)
    if raw_text.is_a?(String)
      @items.push raw_text
    end
  end

  def newline
    @items.push ''
  end

  # -------------------------------------------------- #
  # Block-related methods                              #
  # -------------------------------------------------- #

  def atomic(situations = {}, &block)
    bl = InstructionBlock.new (self)
    temp = @instruction_receiver

    @instruction_receiver = bl
    if block != nil
      block.yield
    end
    @instruction_receiver = temp

    @items.push(bl)
  end

  def receive(instruction)
    bl = InstructionBlock.new (self)
    bl.instructions.push(instruction)
    @items.push(bl)
  end

  # -------------------------------------------------- #
  # Group-related methods                              #
  # -------------------------------------------------- #

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

  # -------------------------------------------------- #
  # Memory-related methods                             #
  # -------------------------------------------------- #

  def get_reg_value (string, index = nil)
    if index == nil
      @j_monitor.readRegisterValue(string).getValue()
    else
      @j_monitor.readRegisterValue(string, index).getValue()
    end
  end

  def get_mem_value (string, index = nil)
    if index == nil
      @j_monitor.readMemoryValue(string).getValue()
    else
      @j_monitor.readMemoryValue(string, index).getValue()
    end
  end

  def get_reg_size (string, index = nil)
    if index == nil
      @j_monitor.readRegisterValue(string).getBitSize()
    else
      @j_monitor.readRegisterValue(string, index).getBitSize()
    end
  end

  def get_mem_size (string, index = nil)
    if index == nil
      @j_monitor.readMemoryValue(string).getBitSize()
    else
      @j_monitor.readMemoryValue(string, index).getBitSize()
    end
  end

  def get_reg_bits (string, index = nil)
    if index == nil
      @j_monitor.readRegisterValue(string).toBinString()
    else
      @j_monitor.readRegisterValue(string, index).toBinString()
    end
  end

  def get_mem_bits (string, index = nil)
    if index == nil
      @j_monitor.readMemoryValue(string).toBinString()
    else
      @j_monitor.readMemoryValue(string, index).toBinString()
    end
  end


  # -------------------------------------------------- #
  # TODO: Labels                                       #
  # -------------------------------------------------- #

  def label(name)
    @last_label += 1000
    ll = @last_label

    #@instruction_receiver.
        receive_label(name.to_s, ll)

    p = lambda do
      return ll
    end

    method_name = name
    while Template.respond_to?(method_name)
      method_name = "label_" + method_name
    end
    Template.send(:define_method, method_name, p)
  end

  def receive_label(name, id)
    @labels[name] = [@items.count, id]
    @r_labels[id] = [name, @items.count]
    @items.push (name + ":")
  end

  # -------------------------------------------------- #
  # Test generation and output                         #
  # -------------------------------------------------- #

  # Run... pre, run and post! This method parses the template
  def parse
    pre
    run
    post
  end

  def execute(j_simulator)

    @items.each do |i|
      if i.is_a?(InstructionBlock)
        i.j_build(j_simulator, self)
      end
    end

    # TODO: goto label code goes HERE!!!!!!!!! vvvvvvv
    # when j_call returns not nil - jump to that label!!!!!!

    i = 0
    passthrough = false
    pass_target = 0

    while i < @items.count

      if passthrough
        if i >= pass_target
          passthrough = false
        end
      end

      if @items[i].is_a?(InstructionBlock)
        pc = @items[i].j_call(@r_labels, !passthrough)
        if pc != nil
          if @r_labels.keys.contains(pc)
            pass_target = @r_labels[pc]
            if pass_target > i
              passthrough = true
            else
              passthrough = false
              i = pass_target
              next
            end
          end
        end
      end

      i += 1
    end

    #@items.each do |i|
    #  if i.is_a?(InstructionBlock)
    #    i.j_call(@r_labels, true)
    #  end
    #end
  end

  # This method prints the template to file and/or stdout.
  # Can be optimized for performance boosts, but not critical right now
  def output(filename)
    if filename != nil
#     File.new filename 
      File.open(filename, 'w') do |file|
        @items.each do |i|
          if i.respond_to?(:output, false)
            i.output(file)
          elsif i.is_a?(String)
            file.puts i
          end
        end
      end
    end

    # Print to screen if necessary
    if $TO_STDOUT
      @items.each do |i|
        if i.respond_to?(:outlog, false)
          i.outlog
        elsif i.is_a?(String)
          puts i
        end
      end
    end
  end

end
