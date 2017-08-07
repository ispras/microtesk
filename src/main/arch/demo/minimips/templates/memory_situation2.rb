#
# Copyright 2015 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to mix MMU-related constraints with nML-derived constraints
# in a single instruction sequence.
#
class MemorySituation2Template < MiniMipsBaseTemplate

  def pre
    super

    buffer_preparator(:target => 'L1') {
      la t0, address
      lw t1, 0, t0
    }

    buffer_preparator(:target => 'L2') {
      la t0, address
      lw t1, 0, t0
    }
  end

  def run
    sequence(
        :engines => {
            :memory => {
                :classifier => 'event-based',
                :page_mask => 0x0fff,
                :align => 4,
                :count => 5}
        }) {
      add t2, t3, t4 do
        situation('IntegerOverflow')
      end

      lw s0, 0, t0 do
        situation('memory', :engine => :memory,
                            :base => 'lw.address',
                            :path => constraints(miss('L1'), hit('L2')))
      end
    }.run
  end

end
