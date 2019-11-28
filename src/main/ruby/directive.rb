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

  def initialize(factory)
    @factory = factory
  end

  def align(value)
    value_in_bytes = Directive.alignment_in_bytes(value)
    @factory.newAlign(value, value_in_bytes)
  end

  def org(origin)
    if origin.is_a?(Integer)
      @factory.newOrigin origin
    elsif origin.is_a?(Hash)
      delta = get_attribute origin, :delta
      if !delta.is_a?(Integer)
        raise "delta (#{delta}) must be an Integer."
      end
      @factory.newOriginRelative delta
    else
      raise "origin (#{origin}) must be an Integer or a Hash."
    end
  end

  # By default, align n is interpreted as alignment on 2**n byte border.
  def self.alignment_in_bytes(n)
    2 ** n
  end

end # Directives