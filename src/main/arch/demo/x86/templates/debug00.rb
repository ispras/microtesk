#
# Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

class Debug00 < X86BaseTemplate
  def run
    trace "ax = %x", gpr(0)
    mov_r16i16 ax, IMM16(11)
    mov_r16r16 cx, ax
    trace "ax = %x", gpr(0)
    trace "cx = %x", gpr(1)
    mov_r16i16 dx, IMM16(2)
    trace "dx = %x", gpr(2)
    mul_r16 ax
    trace "ax = %x", gpr(0)
    trace "dx = %x", gpr(2)
    mov_r16i16 bx, IMM16(7)
    mov_r16i16 cx, IMM16(3)
    add_r16r16 bx, cx
    trace "cx = %x", gpr(1)
    trace "bx = %x", gpr(3)
    mov_r16i16 ax, IMM16(2)
    mov_r16i16 cx, IMM16(5)
    sub_r16r16 ax, cx
    trace "ax = %x", gpr(0)
    trace "cx = %x", gpr(1)
    and_r16r16 ax, cx
    trace "ax = %x", gpr(0)
    trace "cx = %x", gpr(1)
    trace "bx = %x", gpr(3)
    or_r16r16 dx, bx
    or_r16i16 ax, IMM16(0xbb)
    and_r16i16 bx, IMM16(0xcc)
    add_r16i16 cx, IMM16(0xdd)
    sub_r16i16 dx, IMM16(0xee)

    mov_r16i16 cx, IMM16(0)
    trace "cx = %x", gpr(1)

    mov_r16i16 ax, IMM16(200)
    mov_r16i16 bx, IMM16(8)
    mov_rsegr16 ds, ax
    if is_rev('I8086') then
      mov_m16r16 ds, RIAM_BX(), ax
      mmov_r16m16 ds, cx, RIAM_BX()
    end
    trace "ax = %x", gpr(0)
    trace "bx = %x", gpr(3)
    trace "cx = %x", gpr(1)
  end

end
