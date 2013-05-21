require_relative "instruction_block"

class Template

  attr_accessor :is_executable, :j_model, :j_monitor, :j_bbf, :j_dg

  def initialize
    super

    # Until I find a way to properly set every subclass's
    # is_executable to true by default and this class's to
    # false by default...
    @is_executable = true

    @core_block = InstructionBlock.new

    @instruction_receiver = @core_block
    @receiver_stack = [@core_block]

    @final_sequences = Array.new
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
    @instruction_receiver.receive name
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
    while bl.hasValue()

      seq = bl.value()

      seq.each_with_index do |inst, i|
        f_labels = inst.getAttribute("f_labels")
        b_labels = inst.getAttribute("b_labels")

        # TODO: STOPPED HERE ---------------------------------------------------------------<<<<<
        #process labels

      end

    end

    # Execute and generate data in the process
    @generated = Array.new


  end

  def output(file)
    # write @final_sequence to file
  end

end