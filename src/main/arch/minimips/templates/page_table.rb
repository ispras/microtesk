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
# This test template demonstrates how a page table can be defined.
#
class PageTableTemplate < MiniMipsBaseTemplate

  def pre
    super

    page_table {
      page_table_preparator { |pte|
        word pte.v, pte.va, pte.pa
      }

      page_table_adapter { |mo|
        page_table_entry(:v  => 1, :va => mo.va, :pa => mo.pa)
      }

      org 0x000F0000
      label :page_table

      page_table_entry(:v => 1, :va => 0x0000000, :pa => 0x0001000)
      page_table_entry(:v => 1, :va => 0x0001000, :pa => 0x0002000)
      page_table_entry(:v => 1, :va => 0x0002000, :pa => 0x0003000)

      page_table_entry(
        memory_object(
          :va => 0x000A000..0x000F000, :pa => 0x000F000, :size => 64)
      )
    }

  end

  def run
    # Place holder
    nop
  end

end
