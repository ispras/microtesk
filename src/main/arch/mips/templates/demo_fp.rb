#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# demo_fp.rb, Sep 22, 2014 5:06:26 PM
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

#
# Description:
#  
# Sample test template that demonstrates working with
# MIPS floating-point instructions.  
#

class MipsDemo < Template

  def initialize
    super
    @is_executable = true
  end

  def run
    addi 4, REG_IND_ZERO(0), IMM16(8)
    addi 5, REG_IND_ZERO(0), IMM16(16)

    mtc1 REG_IND(4), 1
    mtc1 REG_IND(5), 2

    ADD_S 1, 1, 1
    SUB_S 3, 2, 1

    ADD_D 4, 1, 1
    SUB_D 5, 2, 2

    print_all_fprs
  end

  def fpr(index)
    location('FPR', index)
  end

  def print_all_fprs
    trace "\nDEBUG: FPR values:"
    (0..31).each { |i| trace "FPR[%d] = %s", i, fpr(i) }
    trace ""
  end

end
