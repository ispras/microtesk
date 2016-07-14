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
require_relative 'mmu_plugin'

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

  # Base virtual address (required for direct access to memory)
  attr_reader :base_virtual_address

  # Base physical address (required for direct access to memory)
  attr_reader :base_physical_address

  #
  # Assigns default values to the attributes.
  #
  def initialize
    super

    @sl_comment_starts_with = "//"
    @ml_comment_starts_with = "/*"
    @ml_comment_ends_with   = "*/"

    @indent_token    = "\t"
    @separator_token = "="

    @org_format = ".org 0x%x"
    @align_format = ".align %d"

    @base_virtual_address = 0x0
    @base_physical_address = 0x0
  end

end # Settings

class Template
  include Settings
  include MmuPlugin

  attr_reader :template

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

  def get_caller_location(caller_index = 1)
    # Parses the caller of this method's caller, so the default index is 1
    caller_info = Template.parse_caller(caller[caller_index])
    @template.where File.basename(caller_info[0]), caller_info[1]
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
    blockBuilder.setWhere get_caller_location

    blockBuilder.setAtomic false
    blockBuilder.setSequence false
    blockBuilder.setIterate false

    if attributes.has_key? :combinator
      blockBuilder.setCombinator(attributes[:combinator])
    end

    if attributes.has_key? :permutator
      blockBuilder.setPermutator(attributes[:permutator])
    end

    if attributes.has_key? :compositor
      blockBuilder.setCompositor(attributes[:compositor])
    end

    if attributes.has_key? :rearranger
      blockBuilder.setRearranger(attributes[:rearranger])
    end

    if attributes.has_key? :obfuscator
      blockBuilder.setObfuscator(attributes[:obfuscator])
    end

    attributes.each_pair do |key, value|
      blockBuilder.setAttribute(key.to_s, value)
    end

    self.instance_eval &contents

    @template.endBlock
  end

  def sequence(attributes = {}, &contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setWhere get_caller_location

    blockBuilder.setAtomic false
    blockBuilder.setSequence true
    blockBuilder.setIterate false

    if attributes.has_key? :obfuscator
      blockBuilder.setObfuscator(attributes[:obfuscator])
    end

    attributes.each_pair do |key, value|
      blockBuilder.setAttribute(key.to_s, value)
    end

    self.instance_eval &contents
    @template.endBlock
  end

  def atomic(attributes = {}, &contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setWhere get_caller_location

    blockBuilder.setAtomic true
    blockBuilder.setSequence false
    blockBuilder.setIterate false

    if attributes.has_key? :obfuscator
      blockBuilder.setObfuscator(attributes[:obfuscator])
    end

    attributes.each_pair do |key, value|
      blockBuilder.setAttribute(key.to_s, value)
    end

    self.instance_eval &contents
    @template.endBlock
  end

  def iterate(attributes = {}, &contents)
    blockBuilder = @template.beginBlock
    blockBuilder.setWhere get_caller_location

    blockBuilder.setAtomic false
    blockBuilder.setSequence false
    blockBuilder.setIterate true

    if attributes.has_key? :obfuscator
      blockBuilder.setObfuscator(attributes[:obfuscator])
    end

    if attributes.has_key? :rearranger
      blockBuilder.setRearranger(attributes[:rearranger])
    end

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
    dist.java_object
  end

  def set_default_situation(names, &situations)
    if !names.is_a?(String) and !names.is_a?(Array)
      raise MTRubyError, "#{names} must be String or Array."
    end

    default_situation = self.instance_eval &situations
    if names.is_a?(Array)
      names.each do |name|
        @template.setDefaultSituation name, default_situation
      end
    else
      @template.setDefaultSituation names, default_situation
    end
  end

  #
  # Creates an object for generating a random integer (to be used as an argument of a mode or op)
  # selected from the specified range or according to the specified distribution.
  # 
  def rand(*args)
    if args.count == 1
      distribution = args.at(0)

      if !distribution.is_a?(Dist)
        raise MTRubyError, "the argument must be a distribution."
      end

      @template.newRandom distribution.java_object
    elsif args.count != 2
      from = args.at(0)
      to = args.at(1)

      if !from.is_a?(Integer) or !to.is_a?(Integer)
        raise MTRubyError, "the arguments must be integers."
      end

      @template.newRandom from, to
    else
      raise MTRubyError, "Wrong argument count: #{args.count}. Must be 1 or 2."
    end
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
  def _(allocator = nil, attrs = {})
    if allocator.is_a? Hash and attrs.empty? then
      attrs = allocator
      allocator = nil
    end

    if !attrs.is_a?(Hash)
      raise MTRubyError, "#{attrs} is not a Hash."
    end

    exclude = attrs[:exclude]
    @template.newUnknownImmediate get_caller_location, allocator, exclude
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

  def mode_allocator(name, attrs = {})
    builder = @template.newAllocatorBuilder name

    attrs.each_pair do |key, value|
      builder.setAttribute key.to_s, value.to_s
    end

    builder.build
  end

  def free_allocated_mode(mode)
    @template.freeAllocatedMode mode, false
  end

  def free_all_allocated_modes(mode)
    @template.freeAllocatedMode mode, true
  end

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
  # Creating Preparators and Comparators                                       #
  # -------------------------------------------------------------------------- #

  def preparator(attrs, &contents)
    create_preparator(false, attrs, &contents)
  end

  def comparator(attrs, &contents)
    create_preparator(true, attrs, &contents)
  end

  def create_preparator(is_comparator, attrs, &contents)
    target = get_attribute attrs, :target

    builder = @template.beginPreparator target.to_s, is_comparator
    builder.setWhere get_caller_location 2

    name = attrs[:name]
    if !name.nil?
      builder.setName name.to_s
    end

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

  def variant(attrs = {}, &contents)
    name = attrs[:name]
    bias = attrs[:bias]

    @template.beginPreparatorVariant name, bias
    self.instance_eval &contents
    @template.endPreparatorVariant
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

  def prepare(target_mode, value_object, attrs = {})
    preparator_name = attrs[:name]
    if !preparator_name.nil?
      preparator_name = preparator_name.to_s
    end

    variant_name = attrs[:variant]
    if !variant_name.nil?
      variant_name = variant_name.to_s
    end

    @template.addPreparatorCall target_mode, value_object, preparator_name, variant_name
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

  def entry(*args)
    if args.count == 2
      @template.newEntryReference args.at(0), args.at(1)
    else
      unless defined? @entry_reference
        @entry_reference = BufferEntryReference.new @template
      end

      @entry_reference
    end
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
    if nil != @data_manager
      raise MTRubyError, "Data configuration is already defined"
    end

    text   = get_attribute attrs, :text
    target = get_attribute attrs, :target

    # Default value is 8 bits if other value is not explicitly specified
    addressableSize = attrs.has_key?(:item_size) ? attrs[:item_size] : 8
    baseVirtAddr = attrs.has_key?(:base_virtual_address) ? attrs[:base_virtual_address] : nil

    @data_manager = DataManager.new(self, @template.getDataManager)
    @data_manager.beginConfig text, target, addressableSize, baseVirtAddr

    @data_manager.instance_eval &contents
    @data_manager.endConfig
  end

  def data(attrs = {}, &contents)
    if nil == @data_manager
      raise MTRubyError, "Data configuration is not defined"
    end

    if attrs.has_key?(:global)
      global = attrs[:global]
    else
      global = false
    end

    if attrs.has_key?(:separate_file)
      separate_file = attrs[:separate_file]
    else
      separate_file = false
    end

    @data_manager.beginData global, separate_file
    @data_manager.instance_eval &contents
    @data_manager.endData
  end

  # -------------------------------------------------------------------------- #
  # Code Allocation Facilities                                                 #
  # -------------------------------------------------------------------------- #

  def org(origin)
    if origin.is_a?(Integer)
      @template.setOrigin origin
    elsif origin.is_a?(Hash)
      delta = get_attribute origin, :delta
      if !delta.is_a?(Integer)
        raise MTRubyError, "delta (#{delta}) must be an Integer."
      end
      @template.setRelativeOrigin delta
    else
      raise MTRubyError, "origin (#{origin}) must be an Integer or a Hash."
    end
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

    if attrs.has_key?(:global)
      global = attrs[:global]
    else
      global = false
    end

    if attrs.has_key?(:separate_file)
      separate_file = attrs[:separate_file]
    else
      separate_file = false
    end

    @data_manager.beginData global, separate_file

    page_table = PageTable.new self, @data_manager
    page_table.instance_eval &contents

    @data_manager.endData
  end

  # -------------------------------------------------------------------------- #
  # Generation (Execution and Printing)                                        #
  # -------------------------------------------------------------------------- #

  def generate
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    java_import Java::Ru.ispras.microtesk.test.TestSettings

    engine = TestEngine.getInstance()

    TemplateBuilder.define_runtime_methods engine.getMetaModel

    TestSettings.setCommentToken sl_comment_starts_with
    TestSettings.setIndentToken indent_token
    TestSettings.setSeparatorToken separator_token

    TestSettings.setOriginFormat org_format
    TestSettings.setAlignFormat align_format

    TestSettings.setBaseVirtualAddress base_virtual_address
    TestSettings.setBasePhysicalAddress base_physical_address

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

  def initialize(template, manager)
    @template = template
    @manager = manager

    @builder = nil
    @ref_count = 0
  end

  def beginConfig(text, target, addressableSize, baseVirtAddr)
    @configurer = @manager.beginConfig text, target, addressableSize, baseVirtAddr
  end

  def endConfig
    @manager.endConfig
    @configurer = nil
  end

  def beginData(global, separate_file)
    if @ref_count == 0
      @builder = @template.template.beginData global, separate_file
    end
    @ref_count = @ref_count + 1
    @builder
  end

  def endData
    @ref_count = @ref_count - 1
    if @ref_count == 0
      @template.template.endData
      @builder = nil
    end
  end

  def align(value)
    value_in_bytes = @template.alignment_in_bytes(value)
    @builder.align value, value_in_bytes
  end

  def org(origin)
    if origin.is_a?(Integer)
      @builder.setOrigin origin
    elsif origin.is_a?(Hash)
      delta = get_attribute origin, :delta
      if !delta.is_a?(Integer)
        raise MTRubyError, "delta (#{delta}) must be an Integer."
      end
      @builder.setRelativeOrigin delta
    else
      raise MTRubyError, "origin (#{origin}) must be an Integer or a Hash."
    end
  end

  def type(*args)
    Type.new *args
  end

  def label(id)
    @builder.addLabel id
  end

  def rand(from, to)
    @template.rand from, to
  end

  def define_type(attrs)
    id   = get_attribute attrs, :id
    text = get_attribute attrs, :text
    type = get_attribute attrs, :type

    @configurer.defineType id, text, type.name, type.args

    p = lambda do |*arguments|
      dataBuilder = @builder.addDataValues id
      arguments.each do |value|
        dataBuilder.add value
      end
      dataBuilder.build
    end

    define_method_for DataManager, id, 'type', p
  end

  def define_space(attrs)
    id       = get_attribute attrs, :id
    text     = get_attribute attrs, :text
    fillWith = get_attribute attrs, :fill_with

    @configurer.defineSpace id, text, fillWith

    p = lambda do |length|
      @builder.addSpace length
    end

    define_method_for DataManager, id, 'space', p
  end

  def define_ascii_string(attrs)
    id       = get_attribute attrs, :id
    text     = get_attribute attrs, :text
    zeroTerm = get_attribute attrs, :zero_term

    @configurer.defineAsciiString id, text, zeroTerm

    p = lambda do |*strings|
      @builder.addAsciiStrings zeroTerm, strings
    end

    define_method_for DataManager, id, 'string', p
  end

  def text(value)
    @manager.addText value
  end

  def comment(value)
    @manager.addComment value
  end

  def value(*args)
    @template.value *args 
  end

  def data(attrs = {}, &contents)
    self.instance_eval &contents
  end

  def method_missing(meth, *args, &block)
    # Redirecting call to the template. Note: methods of Template are not accepted.
    if @template.respond_to?(meth) and not Template.instance_methods.include?(meth)
      @template.send meth, *args, &block
    else
      raise MTRubyError, "Method '#{meth}' is not available in data sections."
    end
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

  def text(value)
    @data_manager.text value
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
