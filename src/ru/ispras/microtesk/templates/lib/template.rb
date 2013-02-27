#                              #
#   MicroTESK Ruby front-end   #
#                              #
#        Template class        #
#                              #

require 'fileutils'

class Template
  attr_accessor :is_executable, :target_file

  def initialize
    super

    # Until I find a way to properly set every subclass's
    # is_executable to true by default and this class's to
    # false by default...
    @is_executable = true

    # instruction blocks, labels, other stuff
    @items = Array.new

    @instruction_receiver = self
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
    if(raw_text.is_a?(String))
      @items.push raw_text
    end
  end

  def newline
    @items.push ''
  end

  # -------------------------------------------------- #
  # Block-related methods                              #
  # -------------------------------------------------- #

  def atomic(situations, &block)
    bl = InstructionBlock.new
    temp = @instruction_receiver

    @instruction_receiver = bl
    if(block != nil)
      block.yield
    end
    @instruction_receiver = temp

    @items.push(bl)
  end

  def receive(instruction)
    bl = InstructionBlock.new
    bl.instructions.push(instruction)
    @items.push(bl)
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
      if(i.is_a?(InstructionBlock))
        i.j_build(j_simulator)
        i.j_call
      end
    end
  end

  # This method prints the template to file and/or stdout.
  # Can be optimized for performance boosts, but not critical right now
  def output(filename)
    if(filename != nil)
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