#
# Copyright 2016 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to make dependent instructions with random registers.
#
class RandomRegisterTemplate < MiniMipsBaseTemplate

  def pre
    super

    #
    # Test case level epilogue to return from an exception.
    #
    epilogue {
      nop
    }

    #
    # Start address
    #
    org 0x00020000
  end

  def run
    # Just random registers.
    sequence(:obfuscator => 'random') {
      add reg(_), reg(_), reg(_)
      sub reg(_), reg(_), reg(_)
      slt reg(_), reg(_), reg(_)
    }.run 3

    # Dependency in form of common register objects.
    sequence(:obfuscator => 'random') {
      add x=reg(_),  reg(_), reg(_)
      sub reg(_),    x,      reg(_)
      slt reg(_),    reg(_), x
    }.run 3

    # Dependency in form of common register numbers.
    sequence(:obfuscator => 'random') {
      add reg(x=_), reg(_), reg(_)
      sub reg(_),   reg(x), reg(_)
      slt reg(_),   reg(_), reg(x)
    }.run 3
  end
end
