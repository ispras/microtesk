#
# Copyright 2017 ISP RAS (http://www.ispras.ru)
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

require_relative 'x86_base'

#
# Description:
#
# This test template demonstrates how to make dependent instructions with random registers.
#
class RandomRegisterTemplate < X86BaseTemplate
  def run
    # Just random registers.
    sequence(:obfuscator => 'random') {
      add_r16r16 r16(_), r16(_)
      sub_r16r16 r16(_), r16(_)
      or_r16r16  r16(_), r16(_)
    }.run 3

    # Dependency in form of common register objects.
    sequence(:obfuscator => 'random') {
      add_r16r16 x=r16(_),  r16(_)
      sub_r16r16 r16(_),    x
      or_r16r16  r16(_),    x
    }.run 3

    # Dependency in form of common register numbers.
    sequence(:obfuscator => 'random') {
      add_r16r16 r16(x=_), r16(_)
      sub_r16r16 r16(_),   r16(x)
      or_r16r16  r16(_),   r16(x)
    }.run 3
  end
end
