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
  # Text that starts single-line comments.
  attr_reader :sl_comment_starts_with

  # Text that starts multi-line comments.
  attr_reader :ml_comment_starts_with

  # Text that terminates multi-line comments.
  attr_reader :ml_comment_ends_with

  # Indentation token.
  attr_reader :indent_token

  # Token used in separator lines.
  attr_reader :separator_token

  # Format of the directive responsible for setting origin
  attr_reader :org_format

  # Format of the directive responsible for memory alignment
  attr_reader :align_format

  #
  # Assigns default values to the attributes.
  # 
  def initialize
    @sl_comment_starts_with = "//"
    @ml_comment_starts_with = "/*"
    @ml_comment_ends_with   = "*/"

    @indent_token    = "\t"
    @separator_token = "="

    @org_format = ".org 0x%x"
    @align_format = ".align %d"
  end

end # Settings

class Template
  include Settings

  @@template_classes = Hash.new

  def initialize
    super
  end

  def self.template_classes
    @@template_classes
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

  def atomic(attributes = {}, &contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setAtomic true

    attributes.each_pair do |key, value|
      blockBuilder.setAttribute(key.to_s, value)
    end

    self.instance_eval &contents
    @template.endBlock
  end

  def label(name)
    @template.addLabel name
  end

  def get_address_of(label)
    @template.getAddressForLabel label.to_s
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

  def random_situation(dist)
    dist.next_value
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

    def next_value
      @java_object.value
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

      if value.is_a?(Range)
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
        if bias.nil? then
          builder.addValue value
        else
          builder.addValue value, bias
        end
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
    @template.newUnknownImmediate
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
  # Defining Groups                                                            #
  # -------------------------------------------------------------------------- #

  def define_mode_group(name, distribution)
    if !distribution.is_a?(Dist)
      raise MTRubyError, "#{distribution} is not a distribution."
    end

    @template.defineGroup name, distribution.java_object
    TemplateBuilder.define_addressing_mode_group name
  end

  def define_op_group(name, distribution)
    if !distribution.is_a?(Dist)
      raise MTRubyError, "#{distribution} is not a distribution."
    end

    @template.defineGroup name, distribution.java_object
    TemplateBuilder.define_operation_group name
  end

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
    print_format true, false, format, *args
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
    print_format false, @is_multiline_comment, format, *args
  end

  # 
  # Adds a comment into the test program (uses sl_comment_starts_with).
  #
  def comment(format, *args)
    if sl_comment_starts_with.end_with?(' ')
      print_format false, true, sl_comment_starts_with + format, *args
    else
      print_format false, true, sl_comment_starts_with + ' ' + format, *args
    end
  end

  #
  # Starts a multi-line comment (uses sl_comment_starts_with)
  #
  def start_comment
    @is_multiline_comment = true
    print_format false, true, ml_comment_starts_with
  end

  #
  # Ends a multi-line comment (uses ml_comment_ends_with)
  #
  def end_comment
    print_format false, true, ml_comment_ends_with
    @is_multiline_comment = false
  end

  #
  # Prints a format-based output to the simulator log or to the test program
  # depending of the is_runtime flag.
  #
  def print_format(is_runtime, is_comment, format, *args)
    java_import Java::Ru.ispras.microtesk.test.template.Value

    builder = @template.newOutput is_runtime, is_comment, format

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

  #
  # Creates a pseudo instruction call that prints user-specified text.
  #
  def pseudo(text)
    @template.setCallText text
    @template.endBuildingCall
  end

  # -------------------------------------------------------------------------- #
  # Creating Preparators                                                       #
  # -------------------------------------------------------------------------- #

  def preparator(attrs, &contents)
    target = get_attribute attrs, :target
    builder = @template.beginPreparator target.to_s

    mask = attrs[:mask]
    if !mask.nil?
      if mask.is_a?(String)
        builder.setMaskValue mask
      elsif mask.is_a?(Array)
        builder.setMaskCollection mask
      else
        raise MTRubyError, "Illegal mask type: #{mask}"
      end
    end

    arguments = attrs[:arguments]
    if !arguments.nil?
      if !arguments.is_a?(Hash)
        raise MTRubyError, "#{arguments} is not a Hash."
      end

      arguments.each_pair do |name, value|
        if value.is_a?(Integer)
          builder.addArgumentValue name, value
        elsif value.is_a?(Range)
          builder.addArgumentRange name, value.min, value.max
        elsif value.is_a?(Array)
          builder.addArgumentCollection name, value
        else
          raise MTRubyError, "Illegal value of #{name} argument: #{value}"
        end
      end
    end

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

  # -------------------------------------------------------------------------- #
  # Creating Stream Preparators                                                #
  # -------------------------------------------------------------------------- #

  def stream_preparator(attrs, &contents)
    data  = get_attribute attrs, :data_source
    index = get_attribute attrs, :index_source

    @template.beginStreamPreparator data.to_s, index.to_s

    data_stream_object = StreamPreparator.new self, @template
    data_stream_object.instance_eval &contents

    @template.endStreamPreparator
  end

  def data_source
    @template.getDataSource
  end

  def index_source
    @template.getIndexSource
  end

  def start_label
    @template.getStartLabel
  end

  # -------------------------------------------------------------------------- #
  # Creating Streams                                                           #
  # -------------------------------------------------------------------------- #
  
  def stream(label, data, index, length)
    @template.addStream label.to_s, data, index, length
  end

  # -------------------------------------------------------------------------- #
  # Creating Buffer Preparators                                                #
  # -------------------------------------------------------------------------- #

  def buffer_preparator(attrs, &contents)
    buffer_id = get_attribute attrs, :target
    @template.beginBufferPreparator buffer_id

    self.instance_eval &contents
    @template.endBufferPreparator
  end

  def address(*args)
    if args.count != 0 and args.count != 2
      raise MTRubyError, "Wrong argument count: #{args.count}. Must be 0 or 2."
    end

    if args.count == 2
      @template.newAddressReference args.at(0), args.at(1)
    else
      @template.newAddressReference
    end
  end

  def entry()
    unless defined? @entry_reference
      @entry_reference = BufferEntryReference.new @template
    end

    @entry_reference
  end

  # -------------------------------------------------------------------------- #
  # Generating Data Files                                                      #
  # -------------------------------------------------------------------------- #

  def generate_data(address, label, type, length, method, *flags)
    # puts "Generating data file"
    separate_file = if flags.empty? then true else flags[0] end
    @template.getDataManager.generateData(
      address, label, type, length, method, separate_file)
  end

  # -------------------------------------------------------------------------- #
  # Exception Handling                                                         #
  # -------------------------------------------------------------------------- #

  def exception_handler(attrs = {}, &contents)
    builder = @template.beginExceptionHandler

    exception_handler_object = ExceptionHandler.new self, builder
    exception_handler_object.instance_eval &contents

    @template.endExceptionHandler
  end

  # -------------------------------------------------------------------------- #
  # Data Definition Facilities                                                 #
  # -------------------------------------------------------------------------- #

  def data_config(attrs, &contents)
    #puts "Defining data configuration..."

    if nil != @data_manager
      raise MTRubyError, "Data configuration is already defined"
    end

    text   = get_attribute attrs, :text
    target = get_attribute attrs, :target

    # Default value is 8 bits if other value is not explicitly specified
    addressableSize = attrs.has_key?(:item_size) ? attrs[:item_size] : 8

    @data_manager = DataManager.new(
      self, @template.getDataManager, text, target, addressableSize)

    @data_manager.instance_eval &contents
  end

  def data(attrs = {}, &contents)
    if nil == @data_manager
      raise MTRubyError, "Data configuration is not defined"
    end

    if attrs.has_key?(:separate_file)
      separate_file = attrs[:separate_file]
    else
      separate_file = false
    end

    @template.beginData separate_file
    @data_manager.instance_eval &contents
    @template.endData separate_file
  end

  # -------------------------------------------------------------------------- #
  # Code Allocation Facilities                                                 #
  # -------------------------------------------------------------------------- #

  def org(address)
    @template.setOrigin address
  end

  def align(value)
    value_in_bytes = alignment_in_bytes(value)
    @template.setAlignment value, value_in_bytes
  end

  #
  # By default, align n is interpreted as alignment on 2**n byte border.
  # This behavior can be overridden.
  #
  def alignment_in_bytes(n)
    2 ** n
  end

  # -------------------------------------------------------------------------- #
  # Test Case Level Prologue and Epilogue                                      #
  # -------------------------------------------------------------------------- #

  def prologue(&contents)
    @template.beginPrologue
    self.instance_eval &contents
    @template.endPrologue
  end

  def epilogue(&contents)
    @template.beginEpilogue
    self.instance_eval &contents
    @template.endEpilogue
  end

  # -------------------------------------------------------------------------- #
  # Memory Objects                                                             #
  # -------------------------------------------------------------------------- #

  def memory_object(attrs)
    size = get_attribute attrs, :size
    builder = @template.newMemoryObjectBuilder size

    va = get_attribute attrs, :va
    is_va_label = false

    if va.is_a?(Integer)
      builder.setVa va
    elsif va.is_a?(Range)
      builder.setVa va.min, va.max
    elsif va.is_a?(String) or va.is_a?(Symbol) 
      builder.setVa va.to_s
      is_va_label = true
    else
      raise MTRubyError, "The 'va' attribute has unsupported type #{va.class}."
    end

    if !is_va_label
      pa = get_attribute attrs, :pa
      if pa.is_a?(Integer)
        builder.setPa pa
      elsif pa.is_a?(Range)
        builder.setPa pa.min, pa.max
      elsif pa.is_a?(String) or pa.is_a?(Symbol) 
        builder.setPa pa.to_s
      else
        raise MTRubyError, "The 'pa' attribute has unsupported type #{pa.class}."
      end
    end

    if attrs.has_key?(:name) 
      builder.setName attrs[:name].to_s
    end

    if attrs.has_key?(:mode) 
      builder.setMode attrs[:mode].to_s
    end

    if attrs.has_key?(:data) 
      builder.setData attrs[:data]
    end

    builder.build
  end

  def page_table(attrs = {}, &contents)
    if nil == @data_manager
      raise MTRubyError, "Data configuration is not defined"
    end

    if attrs.has_key?(:separate_file)
      separate_file = attrs[:separate_file]
    else
      separate_file = false
    end

    @template.beginData separate_file

    page_table = PageTable.new self, @data_manager
    page_table.instance_eval &contents

    @template.endData separate_file
  end

  # -------------------------------------------------------------------------- #
  # Generation (Execution and Printing)                                        #
  # -------------------------------------------------------------------------- #

  def generate
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance()

    TemplateBuilder.define_runtime_methods engine.getMetaModel

    engine.setCommentToken sl_comment_starts_with
    engine.setIndentToken indent_token
    engine.setSeparatorToken separator_token

    engine.setOriginFormat org_format
    engine.setAlignFormat align_format

    @template = engine.newTemplate

    @template.beginPreSection
    pre
    @template.endPreSection

    @template.beginPostSection
    post
    @template.endPostSection

    @template.beginMainSection
    run
    @template.endMainSection

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

  def initialize(template, manager, text, target, addressableSize)
    @template = template
    @manager = manager
    @manager.init text, target, addressableSize
  end

  def align(value)
    value_in_bytes = @template.alignment_in_bytes(value)
    @manager.align value, value_in_bytes
  end

  def org(address)
    @manager.setAddress address
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
    fillWith = get_attribute attrs, :fill_with

    @manager.defineSpace id, text, fillWith

    p = lambda do |length|
      @manager.addSpace length
    end

    define_method_for DataManager, id, 'space', p
  end

  def define_ascii_string(attrs)
    id       = get_attribute attrs, :id
    text     = get_attribute attrs, :text
    zeroTerm = get_attribute attrs, :zero_term

    @manager.defineAsciiString id, text, zeroTerm

    p = lambda do |*strings|
      @manager.addAsciiStrings zeroTerm, strings
    end

    define_method_for DataManager, id, 'string', p
  end

end # DataManager

# Methods init, read, write are defined in a separate class to
# avoid name conflicts
#
class StreamPreparator

  def initialize context, template
    @context = context
    @template = template
  end

  def init(&contents)
    @template.beginStreamInitMethod
    @context.instance_eval &contents
    @template.endStreamMethod
  end

  def read(&contents)
    @template.beginStreamReadMethod
    @context.instance_eval &contents
    @template.endStreamMethod
  end

  def write(&contents)
    @template.beginStreamWriteMethod
    @context.instance_eval &contents
    @template.endStreamMethod
  end

end # StreamPreparator

class ExceptionHandler

  def initialize context, builder
    @context = context
    @builder = builder
  end

  def section(attrs = {}, &contents)
    org = get_attribute attrs, :org
    exception = get_attribute attrs, :exception

    @builder.beginSection org, exception
    @context.instance_eval &contents
    @builder.endSection
  end

end # ExceptionHandler

class BufferEntryReference

  def initialize template
    @template = template
  end

  def method_missing(meth, *args)
    if args.count != 0 and args.count != 2
      raise MTRubyError, "Wrong argument count: #{args.count}. Must be 0 or 2."
    end

    if args.count == 2
      @template.newEntryFieldReference meth.to_s, args.at(0), args.at(1)
    else
      @template.newEntryFieldReference meth.to_s
    end
  end
end # BufferEntry

class PageTable

  def initialize(template, data_manager)
    @template = template
    @data_manager = data_manager
  end

  def page_table_preparator(&contents)
    @preparator = contents
  end

  def page_table_adapter(&contents)
    @adapter = contents
  end

  def org(address)
    @data_manager.org address
  end

  def align(value)
    @data_manager.align value
  end

  def label(id)
    @data_manager.label id
  end

  def memory_object(attrs)
    @template.memory_object attrs
  end

  def page_table_entry(attrs)
    java_import Java::Ru.ispras.microtesk.test.template::MemoryObject

    if attrs.is_a?(Hash)
      unless defined? @preparator
        raise MTRubyError, "page_table_preparator is not defined."
      end

      prep = @preparator
      @data_manager.instance_exec(Entry.new(attrs), &prep)
    elsif attrs.is_a?(MemoryObject)
      unless defined? @adapter
        raise MTRubyError, "page_table_adapter is not defined."
      end
      @adapter.call attrs
    else
      raise MTRubyError,
        "Unsupported class of page_table_entry argument: #{attrs.class}"
    end
  end

  class Entry
    def initialize(attrs)
      if !attrs.is_a?(Hash)
        raise MTRubyError, "attrs (#{attrs}) must be a Hash."
      end
      @attrs = attrs
    end

    def method_missing(name, *args)
      if args.count != 0
        raise MTRubyError, "Wrong argument count: #{args.count}. Must be 0."
      end
      @attrs[name.to_sym]
    end
  end # PageTable::Entry

end # PageTable
