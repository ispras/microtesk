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

require ENV['TEMPLATE']

class VLIWDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def pre
    preparator(:target => 'F') {
      comment 'Initializer for F'
      # GPR[25] holds a temporary value
      vliw(
        (lui r(25), value(16, 31)),
        (addi r(25), r(25), value(0, 15))
      )
      vliw(
        (mtf r(25), target),
        nop
      )
    }
  end

  def run
    print_all_fprs

    vliw(
      (add_s f(1), f(3), f(5) do situation('fp.add', :case => 'normal', :exp => 8, :frac => 23) end),
      (add_s f(2), f(4), f(6) do situation('fp.add', :case => 'overflow', :exp => 8, :frac => 23) end) 
    )

    vliw(
      (add_s f(7),  f(9), f(11) do situation('fp.add', :case => 'underflow', :exp => 8, :frac => 23) end),
      (add_s f(8), f(10), f(12) do situation('fp.add', :case => 'inexact', :exp => 8, :frac => 23) end) 
    )

    print_all_fprs
  end
  
  def print_all_fprs
    trace ''
    (1..12).each { |index| print_fpr index }
    trace ''
  end

  def print_fpr(index)
    trace "FPR[%d] = %s", index, fpr(index)
  end

  def fpr(index)
    location('FPR', index)
  end
end
