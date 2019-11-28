####################################################################################################
#
# Copyright 2015-2019 ISP RAS (http://www.ispras.ru)
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

require_relative 'template'

####################################################################################################
# Provides runtime methods to create objects that allow describing test templates for MMU.
####################################################################################################
module MmuPlugin

  def initialize
    super

    java_import Java::Ru.ispras.microtesk.mmu.test.template.ConstraintFactory
    @constraint_factory = ConstraintFactory.get()
  end

  def eq(variable_name, value)
    if value.is_a?(Integer)
      @constraint_factory.newEqValue variable_name, value
    elsif value.is_a?(Array)
      @constraint_factory.newEqArray variable_name, value
    elsif value.is_a?(Range)
      @constraint_factory.newEqRange variable_name, value.min, value.max
    elsif value.is_a?(Dist)
      @constraint_factory.newEqDist variable_name, value.java_object
    else
      raise "#{value} must be Integer, Array, Range or Dist."
    end
  end

  def hit(buffer_name)
    @constraint_factory.newHit buffer_name
  end

  def miss(buffer_name)
    @constraint_factory.newMiss buffer_name
  end

  def read(buffer_name)
    @constraint_factory.newRead buffer_name
  end

  def write(buffer_name)
    @constraint_factory.newWrite buffer_name
  end

  def region(region_name)
    region_name
  end

  def event(buffer_name, attrs = {})
    hit_bias  = if attrs.has_key?(:hit)  then attrs[:hit]  else 0 end
    miss_bias = if attrs.has_key?(:miss) then attrs[:miss] else 0 end
    @constraint_factory.newEvent buffer_name, hit_bias, miss_bias
  end

  def constraints(*primitives)
    @constraint_factory.newConstraints primitives
  end

end # MmuPlugin
