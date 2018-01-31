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

  def run
    pageTableBase = 0xc000

    64.times do |i|
      vpn = (0x00 + i)
      pfn = (0x10 + i) % 63 + 1
      res = (0x0f & i)

      entryData = (vpn << 10) | (pfn << 4) | (res << 0)
      entryAddr = pageTableBase + (i << 1)

      prepare reg(0), entryData
      prepare reg(1), entryAddr

      st reg(0), reg(1)
    end

    prepare reg(10), 0xdead
    prepare reg(11), 0x1234
    st reg(10), reg(11)

    prepare reg(11), 0xd234
    ld reg(10), reg(11)
  end

end
