#
# Copyright 2018-2020 ISP RAS (http://www.ispras.ru)
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

class Debug01 < X86BaseTemplate
  def run
    mov_r16i16 ax, IMM16(11)
    trace "ax = %x", gpr(0)
    and_r16i16 bx, IMM16(0xcc)
    trace "bx = %x", gpr(3)
    mov_r16r16 cx, ax
    trace "cx = %x", gpr(1)

    add_r16r16 bx, cx
    sub_r16r16 ax, cx
    trace "ax = %x", gpr(0)
    trace "cx = %x", gpr(1)
    trace "bx = %x", gpr(3)
  end

end
