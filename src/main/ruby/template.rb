####################################################################################################
#
# Copyright 2013-2019 ISP RAS (http://www.ispras.ru)
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.
#
####################################################################################################

require_relative 'directive'
require_relative 'mmu_plugin'
require_relative 'operators'
require_relative 'template_builder'
require_relative 'utils'

include TemplateBuilder

####################################################################################################
class Template
  include Operators

  attr_reader :template

  @@template_classes = Hash.new

  def initialize
    super
    @situation_manager = SituationManager.new(self)
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
    raise "Failed to parse #{at}."
  end

  # Hack to allow limited use of caps-locked characters
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

  def define_method(method_name, &method_body)
    method_name = method_name.downcase
    if !Template.method_defined?(method_name)
      Template.send(:define_method, method_name, &method_body)
    else
      puts "Error: Failed to define the #{method_name} method"
    end
  end

  #=================================================================================================
  # Main Template Methods
  #=================================================================================================

  # Prologue
  def pre

  end

  # Main part
  def run
    puts "MicroTESK [Ruby] Warning: Trying to execute the original Template#run"
  end

  # Epilogue
  def post

  end

  #=================================================================================================
  # Instruction Sequences
  #=================================================================================================

  #-------------------------------------------------------------------------------------------------
  # Blocks: block {...}, sequence {...}, atomic {...}, and iterate {...}
  #-------------------------------------------------------------------------------------------------

  def block(attributes = {}, &contents)
    java_import Java::Ru.ispras.microtesk.test.template.Block
    add_new_block Block::Kind::BLOCK, attributes, get_caller_location, &contents
  end

  def sequence(attributes = {}, &contents)
    java_import Java::Ru.ispras.microtesk.test.template.Block
    add_new_block Block::Kind::SEQUENCE, attributes, get_caller_location, &contents
  end

  def atomic(attributes = {}, &contents)
    java_import Java::Ru.ispras.microtesk.test.template.Block
    add_new_block Block::Kind::ATOMIC, attributes, get_caller_location, &contents
  end

  def iterate(attributes = {}, &contents)
    java_import Java::Ru.ispras.microtesk.test.template.Block
    add_new_block Block::Kind::ITERATE, attributes, get_caller_location, &contents
  end

  # Adds the given block to the current template.
  def add_new_block(kind, attributes, where, &contents)
    blockBuilder = @template.beginBlock kind
    blockBuilder.setWhere where

    set_builder_attributes blockBuilder, attributes
    self.instance_eval &contents

    @template.endBlock
  end

  #-------------------------------------------------------------------------------------------------
  # Instruction Attributes: <block> { ... <attribute> {...} ... }
  #-------------------------------------------------------------------------------------------------

  def executed(&contents)
    set_attributes(:executed => true, &contents)
  end

  def nonexecuted(&contents)
    set_attributes(:executed => false, &contents)
  end

  def branches(&contents)
    set_attributes(:branches => true, &contents)
  end

  # Sets the given attributes to the nested operations.
  def set_attributes(attributes, &contents)
    mapBuilder = set_builder_attributes @template.newMapBuilder, attributes
    @template.beginAttributes mapBuilder
    self.instance_eval &contents
    @template.endAttributes
  end

  #-------------------------------------------------------------------------------------------------
  # Block-Level Constraints: <block> { ... constraint {...} ... }
  #-------------------------------------------------------------------------------------------------

  # Adds the given constraint to the current block.
  def constraint(&situations)
    @template.addBlockConstraint(@situation_manager.instance_eval(&situations))
  end

  #=================================================================================================
  # Labels and Addresses
  #=================================================================================================

  def label(name)
    if name.is_a?(Integer)
      if !name.between?(0, 9)
        raise "#{name} is must be within the range 0..9"
      end
      @template.addNumericLabel name
    else
      @template.addLabel name.to_s, false
    end
  end

  def global_label(name)
    @template.addLabel name, true
  end

  def weak(name)
    @template.addWeakLabel name
  end

  def label_b(index)
    numeric_label_ref index, false
  end

  def label_f(index)
    numeric_label_ref index, true
  end

  def get_address_of(label)
    @template.getAddressForLabel label.to_s
  end

  #=================================================================================================
  # Situations
  #=================================================================================================

  def situation(name, attrs = {})
    java_import Java::Ru.ispras.microtesk.test.template.Situation
    get_new_situation name, attrs, Situation::Kind::SITUATION
  end

  def testdata(name, attrs = {})
    java_import Java::Ru.ispras.microtesk.test.template.Situation
    get_new_situation name, attrs, Situation::Kind::TESTDATA
  end

  def allocation(name, attrs = {})
    java_import Java::Ru.ispras.microtesk.test.template.Situation
    allocation_data = get_allocation_data nil, attrs
    get_new_situation name, {:allocation => allocation_data}, Situation::Kind::ALLOCATION
  end

  def get_new_situation(name, attrs, kind)
    if !attrs.is_a?(Hash)
      raise "attrs (#{attrs}) must be a Hash"
    end

    builder = @template.newSituation name, kind
    attrs.each_pair do |name, value|
      if value.is_a?(Dist) then
        attr_value = value.java_object
      elsif value.is_a?(Symbol) then
        attr_value = value.to_s
      else
        attr_value = value
      end
      builder.setAttribute name.to_s, attr_value
    end

    builder.build
  end

  def get_allocation_data(allocator, attrs)
    if !attrs.is_a?(Hash)
      raise "attrs (#{attrs}) must be a Hash"
    end

    retain = attrs[:retain]
    exclude = attrs[:exclude]

    track = attrs.has_key?(:track) ? attrs[:track] : -1

    readAfterRate = attrs.has_key?(:read) ? attrs[:read] : attrs[:rate]
    writeAfterRate = attrs.has_key?(:write) ? attrs[:write] : attrs[:rate]

    reserved = attrs.has_key?(:reserved) ? attrs[:reserved] : false

    allocator = @default_allocator if allocator.nil?

    @template.newAllocationData(
      get_caller_location,
      allocator,
      retain,
      exclude,
      track,
      readAfterRate,
      writeAfterRate,
      reserved)
  end

  def random_situation(dist)
    dist.java_object
  end

  def set_default_situation(names, &situations)
    if !names.is_a?(String) and !names.is_a?(Array)
      raise "#{names} must be String or Array"
    end

    default_situation = @situation_manager.instance_eval &situations
    if names.is_a?(Array)
      names.each do |name|
        @template.setDefaultSituation name, default_situation
      end
    else
      @template.setDefaultSituation names, default_situation
    end
  end

  # Creates an object for generating a random integer (to be used as an argument of a mode or op)
  # selected from the specified range or according to the specified distribution.
  def rand(*args)
    if args.count == 1
      distribution = args.at(0)

      if !distribution.is_a?(Dist)
        raise "the argument must be a distribution"
      end

      @template.newRandom distribution.java_object
    elsif args.count == 2
      from = args.at(0)
      to = args.at(1)

      if !from.is_a?(Integer) or !to.is_a?(Integer)
        raise "the arguments must be integers"
      end

      @template.newRandom from, to
    else
      raise "Wrong argument count: #{args.count}. Must be 1 or 2"
    end
  end

  # Creates an object describing the probability distribution for random generation
  # (biased generation). Methods arguments specify ranges of values with corresponding biases.
  def dist(*ranges)
    if !ranges.is_a?(Array)
      raise "#{ranges} is not an Array"
    end

    builder = @template.newVariateBuilder
    ranges.each do |range_item|
      if !range_item.is_a?(ValueRange)
        raise "#{range_item} is not a ValueRange"
      end

      value = range_item.value
      bias = range_item.bias

      if value.is_a?(Range)
        min = [value.first, value.last].min
        max = [value.first, value.last].max
        if bias.nil? then
          builder.addInterval min, max
        else
          builder.addInterval min, max, bias
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

  # Creates an object describing a value range (with corresponding bias) used in random generation.
  # If the bias attribute is not specified, it will be set to nil, which means the default bias.
  def range(attrs = {})
    if !attrs.is_a?(Hash)
      raise "#{attrs} is not a Hash"
    end

    if !attrs.has_key?(:value)
      raise "The :value attribute is not specified in #{attrs}"
    end
    value = attrs[:value]

    bias = nil
    if attrs.has_key?(:bias)
      bias = attrs[:bias]
      if !bias.is_a?(Integer)
        raise "#{bias} is not an Integer"
      end
    end

    ValueRange.new value, bias
  end

  # Creates an object that specifies an unknown immediate value to be used as an argument of a mode
  # or op. A corresponding concrete value must be produced as a result of test data generation for
  # some test situation.
  def _(allocator = nil, attrs = {})
    if allocator.is_a? Hash and attrs.empty? then
      attrs = allocator
      allocator = nil
    end

    if !attrs.is_a?(Hash)
      raise "#{attrs} is not a Hash"
    end

    allocation_data = get_allocation_data allocator, attrs
    @template.newUnknownImmediate(allocation_data)
  end

  # Creates a placeholder for label to be updated in the process of generation.
  def _label
    @template.newLazyLabel
  end

  #=================================================================================================
  # Register Allocation
  #=================================================================================================

  def self.mode_allocator(name)
    java_import Java::Ru.ispras.microtesk.test.template.AllocatorBuilder
    allocator = AllocatorBuilder::newAllocator name
  end

  RANDOM    = mode_allocator('RANDOM')
  FREE      = mode_allocator('FREE')
  USED      = mode_allocator('USED')
  READ      = mode_allocator('READ')
  WRITE     = mode_allocator('WRITE')
  TRY_FREE  = mode_allocator('TRY_FREE')
  TRY_USED  = mode_allocator('TRY_USED')
  TRY_READ  = mode_allocator('TRY_READ')
  TRY_WRITE = mode_allocator('TRY_WRITE')
  BIASED    = mode_allocator('BIASED')

  def set_default_allocator(allocator)
    @default_allocator = allocator
  end

  def set_free(mode, flag)
    @template.addAllocatorAction mode, 'FREE', flag, false
  end

  def set_free_all(mode, flag)
    @template.addAllocatorAction mode, 'FREE', flag, true
  end

  def set_reserved(mode, flag)
    @template.addAllocatorAction mode, 'RESERVED', flag, false
  end

  #=================================================================================================
  # Defining Groups
  #=================================================================================================

  def define_mode_group(name, distribution)
    if !distribution.is_a?(Dist)
      raise "#{distribution} is not a distribution"
    end

    @template.defineGroup name, distribution.java_object
    TemplateBuilder.define_addressing_mode_group name
  end

  def define_op_group(name, distribution)
    if !distribution.is_a?(Dist)
      raise "#{distribution} is not a distribution"
    end

    @template.defineGroup name, distribution.java_object
    TemplateBuilder.define_operation_group name
  end

  #=================================================================================================
  # Text and Debug
  #=================================================================================================

  # Creates a location-based format argument for format-like output methods.
  def location(name, index)
    Location.new name, index
  end

  # Prints text into the simulator execution log.
  def trace(format, *args)
    print_format :TRACE, format, *args
  end

  # Adds the new line character into the test program.
  def newline
    text ''
  end

  # Adds text into the test program.
  def text(format, *args)
    if @is_multiline_comment
      print_format :COMMENT_ML_BODY, format, *args
    else
      print_format :TEXT, format, *args
    end
  end

  # Adds a comment into the test program (uses sl_comment_starts_with).
  def comment(format, *args)
    print_format :COMMENT, format, *args
  end

  # Starts a multi-line comment (uses sl_comment_starts_with)
  def start_comment
    @is_multiline_comment = true
    print_format :COMMENT_ML_START, ''
  end

  # Ends a multi-line comment (uses ml_comment_ends_with)
  def end_comment
    print_format :COMMENT_ML_END, ''
    @is_multiline_comment = false
  end

  # Prints a format-based output to the simulator log or to the test program depending of the
  # is_runtime flag.
  def print_format(kind, format, *args)
    java_import Java::Ru.ispras.microtesk.test.template.Value
    java_import Java::Ru.ispras.microtesk.test.template.Primitive

    builder = @template.newOutput kind.to_s, format

    args.each do |arg|
      if arg.is_a?(Integer) or arg.is_a?(String) or
         arg.is_a?(TrueClass) or arg.is_a?(FalseClass) or arg.is_a?(Value)
         builder.addArgument arg
      elsif arg.is_a?(Location)
        builder.addArgument arg.name, arg.index
      elsif arg.is_a?(Primitive)
        builder.addArgumentPrimitive arg
      else
        raise "Illegal format argument class #{arg.class}"
      end
    end

    @template.addOutput builder.build
  end

  # Creates a pseudo instruction call that prints user-specified text.
  def pseudo(text)
    @template.setCallText text
    @template.endBuildingCall
  end

  # Adds text to the header of generated files.
  def add_to_header(text)
    java_import Java::Ru.ispras.microtesk.test.Printer
    Printer.addToHeader text
  end

  # Adds text to the footer of generated files.
  def add_to_footer(text)
    java_import Java::Ru.ispras.microtesk.test.Printer
    Printer.addToFooter text
  end

  #=================================================================================================
  # Preparators and Comparators
  #=================================================================================================

  def preparator(attrs, &contents)
    create_preparator(false, attrs, &contents)
  end

  def comparator(attrs, &contents)
    create_preparator(true, attrs, &contents)
  end

  def create_preparator(is_comparator, attrs, &contents)
    target = get_attribute attrs, :target

    builder = @template.beginPreparator target.to_s, is_comparator
    builder.setWhere get_caller_location(2)

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
        raise "Illegal mask type: #{mask}"
      end
    end

    arguments = attrs[:arguments]
    if !arguments.nil?
      if !arguments.is_a?(Hash)
        raise "#{arguments} is not a Hash"
      end

      arguments.each_pair do |name, value|
        if value.is_a?(Integer)
          builder.addArgumentValue name, value
        elsif value.is_a?(Range)
          builder.addArgumentRange name, value.min, value.max
        elsif value.is_a?(Array)
          builder.addArgumentCollection name, value
        else
          raise "Illegal value of #{name} argument: #{value}"
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
      raise "Wrong argument count: #{args.count}. Must be 0 or 2"
    end

    if args.count == 2
      @template.newLazy args.at(0), args.at(1)
    else
      @template.newLazy
    end
  end

  # Sign-extends the specified value (currently, supports only LazyValue objects).
  def sign_extend(value_object, bit_size)
    value_object = value_object.java_object if value_object.is_a? WrappedObject
    value_object.signExtend bit_size
  end

  # Zero-extends the specified value (currently, supports only LazyValue objects).
  def zero_extend(value_object, bit_size)
    value_object = value_object.java_object if value_object.is_a? WrappedObject
    value_object.zeroExtend bit_size
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

    value_object = value_object.java_object if value_object.is_a? WrappedObject
    @template.addPreparatorCall target_mode, value_object, preparator_name, variant_name
  end

  #=================================================================================================
  # Memory Preparators
  #=================================================================================================

  # Uses address and data
  def memory_preparator(attrs, &contents)
    size = get_attribute attrs, :size
    builder = @template.beginMemoryPreparator size
    self.instance_eval &contents
    @template.endMemoryPreparator
  end

  #=================================================================================================
  # Data Streams (see also StreamPreparator)
  #=================================================================================================

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

  def stream(label, data, index, length)
    @template.addStream label.to_s, data, index, length
  end

  #=================================================================================================
  # Buffer Preparators
  #=================================================================================================

  def buffer_preparator(attrs, &contents)
    buffer_id = get_attribute attrs, :target
    builder = @template.beginBufferPreparator buffer_id

    if attrs.has_key?(:levels)
      builder.setLevels attrs[:levels]
    end

    self.instance_eval &contents
    @template.endBufferPreparator
  end

  def address(*args)
    if args.count != 0 and args.count != 2
      raise "Wrong argument count: #{args.count}. Must be 0 or 2"
    end

    reference = AddressReference.new @template
    if args.count == 2
      reference.bits args[0], args[1]
    else
      reference
    end
  end

  def entry(*args)
    if args.count != 0 and args.count != 2
      raise "Wrong argument count: #{args.count}. Must be 0 or 2"
    end

    reference = BufferEntryReference.new @template
    if args.count == 2
      reference.bits args[0], args[1]
    else
      reference
    end
  end

  #=================================================================================================
  # Data Files
  #=================================================================================================

  def generate_data(address, label, type, length, method, *flags)
    # puts "Generating data file"
    separate_file = if flags.empty? then true else flags[0] end
    @template.generateData address, label, type, length, method, separate_file
  end

  #=================================================================================================
  # Exception Handling
  #=================================================================================================

  def exception_handler(attrs = {}, &contents)
    if attrs.has_key?(:id)
      id = attrs[:id]
    else
      id = ''
    end

    builder = @template.beginExceptionHandler id
    if attrs.has_key?(:instance)
      instance = attrs[:instance]
    else
      instance = 0..(get_option_value('instance-number').to_i - 1)
    end

    if instance.is_a?(Range)
      builder.setInstances instance.min, instance.max
    else
      builder.setInstances instance
    end

    exception_handler_object = ExceptionHandler.new self, builder
    exception_handler_object.instance_eval &contents

    @template.endExceptionHandler
  end

  #=================================================================================================
  # Data Definition
  #=================================================================================================

  def data_config(attrs, &contents)
    if nil != @data_manager
      raise "Data configuration is already defined"
    end

    target = get_attribute attrs, :target

    # Default value is 8 bits if other value is not explicitly specified
    addressableSize = attrs.has_key?(:item_size) ? attrs[:item_size] : 8

    @data_manager = DataManager.new(self)
    @data_manager.beginConfig target, addressableSize

    @data_manager.instance_eval &contents
    @data_manager.endConfig
  end

  def data(attrs = {}, &contents)
    if nil == @data_manager
      raise "Data configuration is undefined"
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

  #=================================================================================================
  # Assembler Directives (Code)
  #=================================================================================================

  def align(value, fill_with=-1)
    @template.addDirective @directive.align(value, fill_with), get_caller_location
  end

  def balign(value, fill_with=-1)
    @template.addDirective @directive.balign(value, fill_with), get_caller_location
  end

  def p2align(value, fill_with=-1)
    @template.addDirective @directive.p2align(value, fill_with), get_caller_location
  end

  def org(value, is_absolute=false)
    @template.addDirective @directive.org(value, is_absolute), get_caller_location
  end

  def option(value)
    @template.addDirective @directive.option(value), get_caller_location
  end

  #=================================================================================================
  # Sections
  #=================================================================================================

  def section(attrs, &contents)
    name = get_attribute attrs, :name
    prefix = attrs.has_key?(:prefix) ? attrs[:prefix] : ''

    pa   = attrs[:pa]
    va   = attrs[:va]
    args = attrs.has_key?(:args) ? attrs[:args] : ''
    file = attrs.has_key?(:file) ? attrs[:file] : false

    @template.beginSection name, prefix, pa, va, args, file
    self.instance_eval &contents
    @template.endSection
  end

  def section_text(attrs = {}, &contents)
    prefix = attrs.has_key?(:prefix) ? attrs[:prefix] : ''

    pa   = attrs[:pa]
    va   = attrs[:va]
    args = attrs.has_key?(:args) ? attrs[:args] : ''

    @template.beginSectionText prefix, pa, va, args
    self.instance_eval &contents
    @template.endSection
  end

  def section_data(attrs = {}, &contents)
    prefix = attrs.has_key?(:prefix) ? attrs[:prefix] : ''

    pa   = attrs[:pa]
    va   = attrs[:va]
    args = attrs.has_key?(:args) ? attrs[:args] : ''

    @template.beginSectionData prefix, pa, va, args
    self.instance_eval &contents
    @template.endSection
  end

  #=================================================================================================
  # Test-Case-Level Prologue and Epilogue                                                          #
  #=================================================================================================

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

  #=================================================================================================
  # Memory Objects
  #=================================================================================================

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
      raise "The 'va' attribute has unsupported type #{va.class}"
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
        raise "The 'pa' attribute has unsupported type #{pa.class}"
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
      raise "Data configuration is not defined"
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

  #=================================================================================================
  # Test Generation
  #=================================================================================================

  def generate
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance()

    TemplateBuilder.define_runtime_methods engine.getModel.getMetaData
    @template = engine.newTemplate
    @directive = Directive.new(self)

    @template.beginPreSection
    pre
    @template.endPreSection

    @template.beginPostSection
    post
    @template.endPostSection

    @template.beginMainSection
    run
    @template.endMainSection
  end

  def set_option_value(name, value)
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance
    engine.setOptionValue name, value
  end

  def get_option_value(name)
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance
    engine.getOptionValue name
  end

  def rev_id
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance
    engine.getModel.getRevisionId
  end

  def is_rev(id)
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    engine = TestEngine.getInstance
    engine.isRevision id
  end

  #=================================================================================================
  # Private Methods
  #=================================================================================================

  private

  def set_builder_attributes(builder, attributes)
    attributes.each_pair do |key, value|
      if value.is_a?(Hash) then
        mapBuilder = set_builder_attributes @template.newMapBuilder, value
        builder.setAttribute key.to_s, mapBuilder.getMap
      else
        builder.setAttribute key.to_s, if value.is_a?(Symbol) then value.to_s
                                                              else value end
      end
    end
    builder
  end

  def numeric_label_ref(index, forward)
    if !index.is_a?(Integer)
      raise "#{index} is not an Integer"
    end

    if !index.between?(0, 9)
      raise "#{index} is must be within the range 0..9"
    end

    @template.newNumericLabelRef index, forward
  end

end # Template

####################################################################################################
# Describes a value range with corresponding bias used in random generation.
####################################################################################################
class ValueRange
  attr_reader :value, :bias
  def initialize(value, bias)
    @value = value
    @bias = bias
  end
end # ValueRange

####################################################################################################
# Describes the probability distribution for random generation.
# This is a wrapper around the corresponding Java object.
####################################################################################################
class Dist
  attr_reader :java_object
  def initialize(java_object)
    @java_object = java_object
  end

  def next_value
    @java_object.value
  end
end # Dist

####################################################################################################
# Describes an access to a specific location (register or memory) performed when printing data.
####################################################################################################
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

####################################################################################################
class SituationManager
  include MmuPlugin

  def initialize(template)
    super()
    @template = template
  end

  def method_missing(meth, *args, &block)
    # Redirecting call to the template.
    if @template.respond_to?(meth)
      @template.send meth, *args, &block
    else
      raise "Method '#{meth}' is not available in data sections"
    end
  end
end # SituationManager

####################################################################################################
class DataManager
  class Type
    attr_reader :name
    attr_reader :args

    def initialize(*args)
      @name = args[0]
      @args = args.length > 1 ? args[1..args.length-1] : []
    end
  end # Type

  def initialize(template)
    @template = template
    @manager = template.template.getDataManager
    @directive = Directive.new(template)

    @builder = nil
    @ref_count = 0
  end

  def beginConfig(target, addressableSize)
    @configurator = @manager.beginConfig target, addressableSize
  end

  def endConfig
    @manager.endConfig
    @configurator = nil
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

  #=================================================================================================
  # Assembler Directives (Data)
  #=================================================================================================

  def align(value, fill_with=-1)
    @builder.addDirective @directive.align(value, fill_with)
  end

  def balign(value, fill_with=-1)
    @builder.addDirective @directive.balign(value, fill_with)
  end

  def p2align(value, fill_with=-1)
    @builder.addDirective @directive.p2align(value, fill_with)
  end

  def org(value, is_absolute=false)
    @builder.addDirective @directive.org(value, is_absolute)
  end

  def option(value)
    @builder.addDirective @directive.option(value)
  end

  def text(value)
    @builder.addDirective @directive.text(value)
  end

  def comment(value)
    @builder.addDirective @directive.comment(value)
  end

  def label(id)
    @builder.addLabel id, false
  end

  def global_label(id)
    @builder.addLabel id, true
  end

  def define_type(attrs)
    id     = get_attribute attrs, :id
    text   = get_attribute attrs, :text
    type   = get_attribute attrs, :type
    format = attrs.has_key?(:format) ? attrs[:format] : ''
    align  = attrs.has_key?(:align)  ? attrs[:align]  : true

    @configurator.defineType id, text, type.name, type.args, format, align

    # Defining data in data sections
    p = lambda { |*values| @builder.addDirective @directive.data(id, values, align) }
    define_method_for DataManager, id, 'type', p

    # Defining data in code sections
    p = lambda { |*values| @template.addDirective @directive.data(id, values, align),
                                                  get_caller_location }
    define_method_for Template, id, 'type', p
  end

  def define_space(attrs)
    id   = get_attribute attrs, :id
    text = get_attribute attrs, :text
    data = get_attribute attrs, :fill_with

    # Defining data in data sections
    p = lambda { |length| @builder.addDirective @directive.space(text, data, length) }
    define_method_for DataManager, id, 'space', p

    # Defining data in code sections
    p = lambda { |length| @template.addDirective @directive.space(text, data, length),
                                                 get_caller_location }
    define_method_for Template, id, 'space', p
  end

  def define_string(attrs)
    id   = get_attribute attrs, :id
    text = get_attribute attrs, :text
    term = get_attribute attrs, :zero_term

    # Define data in data sections
    p = lambda { |*strings| @builder.addDirective @directive.ascii(text, term, strings) }
    define_method_for DataManager, id, 'string', p

    # Define data in data sections
    p = lambda { |*strings| @template.addDirective @directive.ascii(text, term, strings),
                                                   get_caller_location }
    define_method_for Template, id, 'string', p
  end

  def type(*args)
    Type.new *args
  end

  def rand(from, to)
    @template.rand from, to
  end

  def dist(*ranges)
    @template.dist *ranges
  end

  def range(attrs = {})
    @template.range attrs
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
      raise "Method '#{meth}' is not available in data sections"
    end
  end
end # DataManager

####################################################################################################
# Describes stream methods init, read, write (separate class is to to avoid name conflicts).
####################################################################################################
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

####################################################################################################
class ExceptionHandler
  def initialize context, builder
    @context = context
    @builder = builder
  end

  def entry_point(attrs = {}, &contents)
    org = get_attribute attrs, :org
    exception = get_attribute attrs, :exception

    @builder.beginEntryPoint org, exception
    @context.instance_eval &contents
    @builder.endEntryPoint
  end
end # ExceptionHandler

####################################################################################################
class WrappedObject
  def java_object
    raise NotImplementedError, "Method java_object is not implemented"
  end
end # WrappedObject

####################################################################################################
class AddressReference < WrappedObject
  def initialize(template)
    @template = template
    @level = 0;
  end

  def [](arg)
    @level = arg;
    self
  end

  def java_object
    @template.newAddressReference @level
  end

  def call(min, max)
    bits(min, max)
  end

  def bits(min, max)
    @template.newAddressReference @level, min, max
  end
end # AddressReference

####################################################################################################
class BufferEntryReference < WrappedObject
  def initialize(template)
    @template = template
    @level = 0;
  end

  def [](arg)
    @level = arg;
    self
  end

  def java_object
    @template.newEntryReference @level
  end

  def call(min, max)
    bits(min, max)
  end

  def bits(min, max)
    @template.newEntryReference @level, min, max
  end

  def method_missing(meth, *args)
    if args.count != 0 and args.count != 2
      raise "Wrong argument count: #{args.count}. Must be 0 or 2"
    end

    if args.count == 2
      @template.newEntryFieldReference @level, meth.to_s, args[0], args[1]
    else
      @template.newEntryFieldReference @level, meth.to_s
    end
  end
end # BufferEntryReference

####################################################################################################
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

  def global_label(id)
    @data_manager.global_label id
  end

  def memory_object(attrs)
    @template.memory_object attrs
  end

  def page_table_entry(attrs)
    java_import Java::Ru.ispras.microtesk.test.template::MemoryObject

    if attrs.is_a?(Hash)
      unless defined? @preparator
        raise "page_table_preparator is not defined"
      end

      prep = @preparator
      @data_manager.instance_exec(Entry.new(attrs), &prep)
    elsif attrs.is_a?(MemoryObject)
      unless defined? @adapter
        raise "page_table_adapter is not defined"
      end
      @adapter.call attrs
    else
      raise "Unsupported class of page_table_entry argument: #{attrs.class}"
    end
  end

  class Entry
    def initialize(attrs)
      if !attrs.is_a?(Hash)
        raise "attrs (#{attrs}) must be a Hash"
      end
      @attrs = attrs
    end

    def method_missing(name, *args)
      if args.count != 0
        raise "Wrong argument count: #{args.count}. Must be 0"
      end
      @attrs[name.to_sym]
    end
  end # PageTable::Entry

end # PageTable
