#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

require_relative 'base_template'

class VliwDemo < VliwDemoTemplate

  def initialize
    super
    @is_executable = true
  end

  def run
    trace_all_fprs

    vliw(
      (add_s f(1), f(1), f(2) do situation('fp.add', :case => 'normal', :exp => 8, :frac => 23) end),
      (add_s f(3), f(3), f(4) do situation('fp.add', :case => 'overflow', :exp => 8, :frac => 23) end) 
    )

    vliw(
      (add_s f(5), f(5), f(6) do situation('fp.add', :case => 'underflow', :exp => 8, :frac => 23) end),
      (add_s f(7), f(7), f(8) do situation('fp.add', :case => 'inexact', :exp => 8, :frac => 23) end) 
    )

    vliw(
      (sub_s  f(9),  f(9), f(10) do situation('fp.sub', :case => 'normal', :exp => 8, :frac => 23) end),
      (sub_s f(11), f(11), f(12) do situation('fp.sub', :case => 'overflow', :exp => 8, :frac => 23) end) 
    )

    vliw(
      (sub_s f(13), f(13), f(14) do situation('fp.sub', :case => 'underflow', :exp => 8, :frac => 23) end),
      (sub_s f(15), f(15), f(0) do situation('fp.sub', :case => 'inexact', :exp => 8, :frac => 23) end) 
    )

    trace_all_fprs
  end

  def trace_all_fprs
    trace ''
    (0..15).each { |index| trace_fpr index }
    trace ''
  end

end
