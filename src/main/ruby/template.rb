#
# Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

require_relative 'template_builder'
require_relative 'utils'

include TemplateBuilder

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

  # Seed for the randomizer
  attr_reader :random_seed

  #
  # Assigns default values to the attributes.
  # 
  def initialize
    @use_stdout    = true
    @log_execution = true

    @sl_comment_starts_with = "// "
    @ml_comment_starts_with = "/*"
    @ml_comment_ends_with   = "*/"

    @random_seed = 0
  end

end # Settings

class Template
  include Settings

  @@model = nil
  @@template_classes = Hash.new

  def initialize
    super
  end

  def self.template_classes
    @@template_classes
  end

  def self.set_model(model)
    if nil != @@model
      puts "Model is already assigned."
      return
    end

    TemplateBuilder.define_runtime_methods model.getMetaData
    @@model = model
  end

  # This method adds every subclass of Template to the list of templates to parse
  def self.inherited(subclass)
    subclass_file = parse_caller(caller[0])[0]
    puts "Loaded template #{subclass} defined in #{subclass_file}"
    @@template_classes.store subclass, subclass_file
  end

  # Parses the text of stack entries returned by the "caller" method,
  # which have the following format: <file:line> or <file:line: in `method'>.
  def self.parse_caller(at)
    if /^(.+?):(\d+)(?::in `(.*)')?/ =~ at
      file   = Regexp.last_match[1]
      line   = Regexp.last_match[2].to_i
      method = Regexp.last_match[3]
      return [file, line, method]
    end
    raise MTRubyError, "Failed to parse #{at}."
  end

  # Hack to allow limited use of capslocked characters
  def method_missing(meth, *args, &block)
    if self.respond_to?(meth.to_s.downcase)
      self.send meth.to_s.downcase.to_sym, *args, &block
    else
      super
    end
  end

  # ------------------------------------------------------------------------- #
  # Main template writing methods                                             #
  # ------------------------------------------------------------------------- #

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

  # ------------------------------------------------------------------------- #
  # Methods for template description facilities                               #
  # ------------------------------------------------------------------------- #

  def block(attributes = {}, &contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setAtomic false

    if attributes.has_key? :compositor
      blockBuilder.setCompositor(attributes[:compositor])
    end

    if attributes.has_key? :combinator
      blockBuilder.setCombinator(attributes[:combinator])
    end

    attributes.each_pair do |key, value|
      blockBuilder.setAttribute(key.to_s, value)
    end

    self.instance_eval &contents

    @template.endBlock
  end

  def atomic(&contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setAtomic true

    self.instance_eval &contents
    @template.endBlock
  end

  def label(name)
    @template.addLabel name 
  end

  def address(label)
    @template.getMemoryMap.resolve label.to_s 
  end

  def situation(name, attrs = {})
    if !attrs.is_a?(Hash)
      raise MTRubyError, "attrs (#{attrs}) must be a Hash."
    end

    builder = @template.newSituation name
    attrs.each_pair do |name, value|
      attr_value = if value.is_a?(Dist) then value.java_object else value end
      builder.setAttribute name.to_s, attr_value
    end

    builder.build
  end

  #
  # Creates an object for generating a random integer within
  # the specified range (to be used as an argument of a mode or op).
  # 
  def rand(from, to)
    if !from.is_a?(Integer) or !to.is_a?(Integer)
      raise MTRubyError, "from #{from} and to #{to} must be integers."
    end
    @template.newRandom from, to
  end

  #
  # Describes the probability distribution for random generation.
  # This is a wrapper around the corresponding java object.
  #
  class Dist
    attr_reader :java_object
    def initialize(java_object)
      @java_object = java_object
    end
  end

  #
  # Creates an object describing the probability distribution for
  # random generation (biased generation). Methods arguments
  # specify ranges of values with corresponding biases.
  #
  def dist(*ranges)
    if !ranges.is_a?(Array)
      raise MTRubyError, "#{ranges} is not an Array."
    end

    builder = @template.newVariateBuilder
    ranges.each do |range_item|
      if !range_item.is_a?(ValueRange)
        raise MTRubyError, "#{range_item} is not a ValueRange."
      end

      value = range_item.value
      bias = range_item.bias

      if value.is_a?(Integer)
        if bias.nil? then
          builder.addValue value
        else
          builder.addValue value, bias
        end
      elsif value.is_a?(Range)
        if bias.nil? then
          builder.addInterval value.min, value.max
        else
          builder.addInterval value.min, value.max, bias
        end
      elsif value.is_a?(Array)
        if bias.nil? then
          builder.addCollection value
        else
          builder.addCollection value, bias
        end
      elsif value.is_a?(Dist)
        if bias.nil? then
          builder.addVariate value.java_object
        else
          builder.addVariate value.java_object, bias
        end
      else
        raise MTRubyError, "#{value} has an unsupported type."
      end
    end

    Dist.new builder.build
  end

  #
  # Describes a value range with corresponding biase used in random generation.
  #
  class ValueRange
    attr_reader :value, :bias
    def initialize(value, bias)
      @value = value
      @bias = bias
    end
  end

  #
  # Creates an object describing a value range (with corresponding bias)
  # used in random generation. If the bias attribute is not specified,
  # it will be set to nil, which means the default bias. 
  #
  def range(attrs = {})
    if !attrs.is_a?(Hash)
      raise MTRubyError, "#{attrs} is not a Hash."
    end

    if !attrs.has_key?(:value)
      raise MTRubyError, "The :value attribute is not specified in #{attrs}."
    end
    value = attrs[:value]

    bias = nil
    if attrs.has_key?(:bias)
      bias = attrs[:bias]
      if !bias.is_a?(Integer)
        raise MTRubyError, "#{bias} is not an Integer."
      end
    end

    ValueRange.new value, bias
  end

  #
  # Creates an object that specifies an unknown immediate value to be used
  # as an argument of a mode or op. A corresponding concrete value must be
  # produced as a result of test data generation for some test situation.
  #
  def _
    @template.newUnknown
  end

  # --- Special "no value" method ---
  # Similar to the above method, but the described object is more complex
  # than an immediate value (most likely, it will be some MODE or OP). 
  # TODO: Not implemented. Left as a requirement.
  # Should be implemented in the future.
  #
  # def __(aug_value = nil)
  #   NoValue.new(aug_value)
  # end

  # -------------------------------------------------------------------------- #
  # Printing Text Messages                                                     #
  # -------------------------------------------------------------------------- #

  #
  # Creates a location-based format argument for format-like output methods. 
  #
  def location(name, index)
    Location.new name, index
  end

  #
  # Prints text into the simulator execution log.
  #
  def trace(format, *args)
    print_format true, format, *args
  end

  # 
  # Adds the new line character into the test program
  #
  def newline
    text '' 
  end

  # 
  # Adds text into the test program.
  #
  def text(format, *args)
    print_format false, format, *args
  end
  
  # 
  # Adds a comment into the test program (uses sl_comment_starts_with).
  #
  def comment(format, *args)
    text sl_comment_starts_with + format
  end

  #
  # Starts a multi-line comment (uses sl_comment_starts_with)
  #
  def start_comment
    text ml_comment_starts_with
  end

  #
  # Ends a multi-line comment (uses ml_comment_ends_with)
  #
  def end_comment
    text ml_comment_ends_with 
  end

  #
  # Prints a format-based output to the simulator log or to the test program
  # depending of the is_runtime flag.
  #
  def print_format(is_runtime, format, *args)
    java_import Java::Ru.ispras.microtesk.test.template.Value
    
    builder = @template.newOutput is_runtime, format

    args.each do |arg|
      if arg.is_a?(Integer) or arg.is_a?(String) or 
         arg.is_a?(TrueClass) or arg.is_a?(FalseClass) or arg.is_a?(Value)
         builder.addArgument arg
      elsif arg.is_a?(Location)
        builder.addArgument arg.name, arg.index 
      else
        raise MTRubyError, "Illegal format argument class #{arg.class}"
      end
    end

    @template.addOutput builder.build
  end

  
  
  # -------------------------------------------------------------------------- #
  # Creating Preparators                                                       #
  # -------------------------------------------------------------------------- #

  def preparator(attrs, &contents)
    target = get_attribute attrs, :target
    @template.beginPreparator target.to_s
    self.instance_eval &contents
    @template.endPreparator
  end

  def target
    @template.getPreparatorTarget
  end

  def value(*args)
    if args.count != 0 and args.count != 2
      raise MTRubyError, "Wrong argument count: #{args.count}. Must be 0 or 2."
    end

    if args.count == 2
      @template.newLazy args.at(0), args.at(1)
    else
      @template.newLazy
    end
  end

  # ------------------------------------------------------------------------- #
  # Data Definition Facilities                                                #
  # ------------------------------------------------------------------------- #

  def data_config(attrs, &contents)
    puts "Defining data configuration..."

    if nil != @data_manager
      raise MTRubyError, "Data configuration is already defined"
    end

    text            = get_attribute attrs, :text
    target          = get_attribute attrs, :target
    addressableSize = get_attribute attrs, :addressableSize

    @data_manager = DataManager.new @template.getDataManager, text, target, addressableSize
    @data_manager.instance_eval &contents
  end

  def data(&contents)
    puts "Defining data..."

    if nil == @data_manager
      raise MTRubyError, "Data configuration is not defined"
    end

    @data_manager.instance_eval &contents
  end

  # ------------------------------------------------------------------------- #
  # Generation (Execution and Printing)                                       #
  # ------------------------------------------------------------------------- #

  def generate(filename)
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance(@@model)

    engine.setFileName      filename
    engine.setLogExecution  log_execution
    engine.setPrintToScreen use_stdout
    engine.setCommentToken  sl_comment_starts_with
    engine.setRandomSeed    random_seed

    @template = engine.newTemplate

    @template.beginPreSection
    pre
    @template.endPreSection

    @template.beginMainSection
    run
    @template.endMainSection

    @template.beginPostSection
    post
    @template.endPostSection

    engine.process @template
  end

end # Template

#
# Description:
#
# The Location class describes an access to a specific location (register or
# memory address) performed when printing data.
#
class Location
  attr_reader :name, :index

  def initialize(name, index)
    @name  = name
    @index = index
  end

 def to_s
   "#{@name}[#{@index}]"
 end
end # Location

class DataManager

  class Type
    attr_reader :name
    attr_reader :args

    def initialize(*args)
      @name = args[0]
      @args = args.length > 1 ? args[1..args.length-1] : []
    end
  end

  def initialize(manager, text, target, addressableSize)
    @manager = manager
    @manager.init text, target, addressableSize
  end

  def type(*args)
    Type.new *args
  end

  def label(id)
    @manager.addLabel id
  end

  def rand(from, to)
    if !from.is_a?(Integer) or !to.is_a?(Integer)
      raise MTRubyError, "from #{from} and to #{to} must be integers." 
    end
    @manager.newRandom from, to
  end

  def define_type(attrs)
    id   = get_attribute attrs, :id
    text = get_attribute attrs, :text
    type = get_attribute attrs, :type

    @manager.defineType id, text, type.name, type.args

    p = lambda do |*arguments|
      @manager.addData id, arguments 
    end

    define_method_for DataManager, id, 'type', p
  end

  def define_space(attrs)
    id       = get_attribute attrs, :id
    text     = get_attribute attrs, :text
    fillWith = get_attribute attrs, :fillWith

    @manager.defineSpace id, text, fillWith

    p = lambda do |length|
      @manager.addSpace length
    end

    define_method_for DataManager, id, 'space', p
  end

  def define_ascii_string(attrs)
    id       = get_attribute attrs, :id
    text     = get_attribute attrs, :text
    zeroTerm = get_attribute attrs, :zeroTerm

    @manager.defineAsciiString id, text, zeroTerm

    p = lambda do |*strings|
      @manager.addAsciiStrings zeroTerm, strings
    end

    define_method_for DataManager, id, 'string', p
  end

end # Data
