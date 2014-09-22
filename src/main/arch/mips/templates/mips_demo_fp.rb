#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# mips_demo_fp.rb, Sep 22, 2014 5:06:26 PM
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
    ADD_S 1, 2, 3
    SUB_S 3, 4, 5

    ADD_D 1, 2, 3
    SUB_D 3, 4, 5

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
