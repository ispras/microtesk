#
# Copyright 2018 ISP RAS (http://www.ispras.ru)
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

require_relative 'vmem_base'

class ExampleTemplate < VmemBaseTemplate

  def translateVpn(vpn)
    (0x10 + vpn) % 31 + 1
  end

  def run
    pageTableBase = 0xc000

    64.times do |i|
      vpn = i
      pfn = translateVpn(vpn)
      res = i & 0x0f

      entryData = (vpn << 0) | (pfn << 6) | (res << 12)
      entryAddr = pageTableBase + (i << 1)

      prepare reg(0), entryData
      prepare reg(1), entryAddr

      st reg(0), reg(1)
    end

    data = 0xdead
    addr = 0x1234

    prepare reg(10), data
    prepare reg(11), addr

    st reg(10), reg(11)
    trace "ST: vmem[0x%x] = 0x%x", location("GPR", 11), location("GPR", 10)

    ld reg(10), reg(11)
    trace "LD: vmem[0x%x] = 0x%x", location("GPR", 11), location("GPR", 10)

    addr = (3 << 14) | (translateVpn((addr >> 8) & 0x3f) << 8) | (addr & 0xff)

    prepare reg(11), addr

    ld reg(10), reg(11)
    trace "LD: vmem[0x%x] = 0x%x", location("GPR", 11), location("GPR", 10)
  end

end
