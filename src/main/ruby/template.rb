
# Mixins for the Template class
require_relative "output"

# Other dependencies
require_relative "constructs"
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
  include Output

  @@template_classes = Array.new

  def initialize
    super
   
    # Important variables for core Template functionality
    @core_block = InstructionBlock.new

    @instruction_receiver = @core_block
    @receiver_stack = [@core_block]

    @final_sequences = Array.new
    
    java_import Java::Ru.ispras.microtesk.test.template.BlockId
    @block_id = BlockId.new
  end

  def self.template_classes
    @@template_classes
  end

  def self.set_model(j_model)
    @@model = j_model
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

  # -------------------------------------------------- #
  # Methods for template description facilities        #
  # -------------------------------------------------- #

  def block(attributes = {}, &contents)
    @block_id = @block_id.nextChildId

    b = InstructionBlock.new
    b.attributes = attributes

    @receiver_stack.push b
    @instruction_receiver = @receiver_stack.last

    self.instance_eval &contents

    @receiver_stack.pop
    @instruction_receiver = @receiver_stack.last

    @instruction_receiver.receive b

    @block_id = @block_id.parentId
  end

  def label(name)
    l = Java::Ru.ispras.microtesk.test.template.Label.new(name.to_s, @block_id) 
    @instruction_receiver.receive Label.new(l)
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
  # Generation (Execution and Printing)                #
  # -------------------------------------------------- #

  def generate(filename)
    puts
    puts "--------------------------------- Start build ----------------------------------"
    puts

    pre
    run
    post

    java_import Java::Ru.ispras.microtesk.test.TestEngine

    engine = TestEngine.getInstance(@@model)

    # Apply settings
    engine.setFileName      filename
    engine.setLogExecution  log_execution
    engine.setPrintToScreen use_stdout
    engine.setCommentToken  sl_comment_starts_with

    block_builders = engine.getBlockBuilders 
    bl = @core_block.build engine.getBlockBuilders
    bl_iter = bl.getIterator

    engine.process bl_iter
  end

end
