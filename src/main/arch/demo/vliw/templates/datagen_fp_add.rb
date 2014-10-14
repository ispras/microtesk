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
    preparator(:target => 'R') {
      comment 'Initializer for R'  
      vliw(
        (lui target, value(16, 31)),
        (addi target, target, value(0, 15))
      )
    }

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
    print_fpr 3
    print_fpr 5
    print_fpr 4
    print_fpr 6

    vliw(
      (add_s f(1), f(3), f(5) do situation('fp.add', :case => 'normal', :exp => 8, :frac => 23) end),
      (add_s f(2), f(4), f(6) do situation('fp.add', :case => 'overflow', :exp => 8, :frac => 23) end) 
    )

    print_fpr 1
    print_fpr 2
  end

  def print_gpr(index)
    trace "GPR[%d] = %s", index, gpr(index)
  end

  def print_fpr(index)
    trace "FPR[%d] = %s", index, fpr(index)
  end

  def gpr(index)
    location('GPR', index)
  end

  def fpr(index)
    location('FPR', index)
  end
end
