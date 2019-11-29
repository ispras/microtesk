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
    factory = @template.getDirectiveFactory
    factory.newAlign(value, 2 ** value)
  end

  def balign(value)
    factory = @template.getDirectiveFactory
    factory.newAlignByte(value)
  end

  def p2align(value)
    factory = @template.getDirectiveFactory
    factory.newAlignPower2(value, 2 ** value)
  end

  def org(origin)
    factory = @template.getDirectiveFactory
    if origin.is_a?(Integer)
      factory.newOrigin origin
    elsif origin.is_a?(Hash)
      delta = get_attribute origin, :delta
      if !delta.is_a?(Integer)
        raise "delta (#{delta}) must be an Integer."
      end
      factory.newOriginRelative delta
    else
      raise "origin (#{origin}) must be an Integer or a Hash."
    end
  end

  def option(value)
    factory.newOption(value)
  end

end # Directives