#
# Copyright 2015 ISP RAS (http://www.ispras.ru)
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

require_relative 'minimips_base'

#
# Description:
#
# This test template demonstrates how to generate test cases 
# based on random values. 
#
class RandomTemplate < MiniMipsBaseTemplate

  class RangeItem
    attr_reader :value, :bias
    def initialize(value, bias)
      @value = value
      @bias = bias
    end
  end 

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

    RangeItem.new value, bias
  end

  def dist(*ranges)
    if !ranges.is_a?(Array)
      raise MTRubyError, "#{ranges} is not an Array."
    end

    builder = @template.newVariateBuilder
    ranges.each do |range_item|
      value = range_item.value
      bias = range_item.bias

      if value.is_a?(Integer)
        builder.add value unless nil == bias
        builder.add value, bias if nil == bias
      elsif value.is_a?(Range)
        builder.addInterval value.min, value.max unless nil == bias
        builder.addInterval value.min, value.max, bias if nil == bias
      elsif value.is_a?(Array)
        builder.add value unless nil == bias
        builder.add value, bias if nil == bias
    # elsif value.is_a?(Dist)
    #   puts "Distribution value: #{value}"
      else
        builder.add value unless nil == bias
        builder.add value, bias if nil == bias
      end
    end

    builder.build
  end

  def run
    my_dist = dist(range(:value => 1,         :bias => 1),
                   range(:value => 1..3,      :bias => 2),
                   range(:value => [1, 2, 3], :bias => 3))

    add t0, t1, t2 do situation('random', :size => 32, :min_imm => 1, :max_imm => 31,
      :dist => dist(range(:value=> 1,         :bias => 1), # Single
                    range(:value=> 1..3,      :bias => 2), # Interval
                    range(:value=> [1, 2, 3], :bias => 3), # Collection
                    range(:value=> my_dist,   :bias => 4)) # Distribution
      ) end
  end

end
