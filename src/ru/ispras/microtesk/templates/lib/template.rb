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

    @target_file = nil

    @current_instruction = nil
    @current_situations = Array.new

    @instruction_list = Array.new
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
    @instruction_list.push raw_text
  end

  def newline
    @instruction_list.push ''
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

  # This method prints the template to file and/or stdout.
  # Can be optimized for performance boosts, but not critical right now
  def output(out_dir)
    # Set the target file as the template class name unless it's explicitly stated
    @target_file ||= self.class.name
    @target_file += '.asm' unless @target_file.include? '.'

    # Figure out the full target directory and create it if necessary
    target_dir = File.expand_path(out_dir)
    target_dir += '/' unless target_dir =~ /\/$/
                    # ^ target_dir[target_dir.size-1]=='/'

    check_dir = File.dirname(target_dir + @target_file)
    unless File.directory?(check_dir)
      FileUtils.mkdir_p(check_dir)
    end

    # Print to files if necessary
    if $TO_FILES
      File.open(target_dir + @target_file, 'w') do |file|
        @instruction_list.each do |inst|
          file.puts inst
        end
      end
    end

    # Print to screen if necessary
    if $TO_STDOUT
      @instruction_list.each do |inst|
        puts inst
      end
    end

  end


end