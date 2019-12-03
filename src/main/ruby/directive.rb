####################################################################################################
#
# Copyright 2019 ISP RAS (http://www.ispras.ru)
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

####################################################################################################
# Defines basic assembler directives.
####################################################################################################
class Directive

  def initialize(template)
    @template = template
  end

  def align(value)
    factory = @template.get_directive_factory
    factory.newAlign(value, 2 ** value)
  end

  def balign(value)
    factory = @template.get_directive_factory
    factory.newAlignByte(value)
  end

  def p2align(value)
    factory = @template.get_directive_factory
    factory.newAlignPower2(value, 2 ** value)
  end

  def org(origin, is_code)
    factory = @template.get_directive_factory
    if origin.is_a?(Integer)
      if is_code
        factory.newOriginAbsolute origin # Absolute
      else
        factory.newOrigin origin # Section-Based
      end
    elsif origin.is_a?(Hash)
      delta = get_attribute origin, :delta
      if !delta.is_a?(Integer)
        raise "delta (#{delta}) must be an Integer."
      end
      factory.newOriginRelative delta # Relative
    else
      raise "origin (#{origin}) must be an Integer or a Hash."
    end
  end

  def option(value)
    factory = @template.get_directive_factory
    factory.newOption(value)
  end

  def text(text)
    factory = @template.get_directive_factory
    factory.newText(text)
  end

  def comment(text)
    factory = @template.get_directive_factory
    factory.newComment(text)
  end

  #=================================================================================================
  # The following directives are configured via data_config
  #=================================================================================================

  def data(type, values)
    factory = @template.get_directive_factory
    dataBuilder = factory.getDataValueBuilder type.to_s
    values.each do |value|
      if value.is_a?(Float) then
        dataBuilder.addDouble value
      else
        dataBuilder.add value
      end
    end
    dataBuilder.build
  end

  def space(length)
    factory = @template.get_directive_factory
    factory.newSpace(length)
  end

  def ascii(zero_term, strings)
    factory = @template.get_directive_factory
    factory.newAsciiStrings(zero_term, strings)
  end

  #=================================================================================================

  def method_missing(meth, *args, &block)
    raise "Unknown directive '#{meth}'"
  end

end # Directives