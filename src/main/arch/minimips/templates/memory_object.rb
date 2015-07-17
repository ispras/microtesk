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
# This test template demonstrates how to use the "memory_object" construct.
#
class MemoryObjectTemplate < MiniMipsBaseTemplate

  def pre
    super

    data {
      org 0x00010000
      label :label
      word rand(0, 9)
    }
  end

  def run
    # Attributes:
    #
    # 1. name [optional]   : String or Symbol
    # 2. size [compulsory] : Integer, size int bytes, must be a power of 2
    # 3. mode [optional]   : String "rwx", access mode, by default is "r--" 
    # 4. va   [compulsory] : address | address1..address2 | label
    # 5. pa   [compulsory] : address | address1..address2 | region
    # 6. data [optional]   : Integer
    #
    # NOTE: If va is specified as label, pa is not used and is set to null.

    mo1 = memory_object(
      :va => 0x0002000, :pa => 0x000F000, :size => 8,:data => 0xDEADBEEF)

    mo2 = memory_object(
      :name=> :mo2, :mode => "rw-", :va => 0x0002000, :pa => 0x000F000, :size => 8)

    mo3 = memory_object(
       :va => :label, :size => 8)

    mo4 = memory_object(
      :va => 0x000A000..0x000F000, :pa => 0x000F000, :size => 64)

    mo5 = memory_object(
      :va => 0x0002000, :pa => "data", :size => 8)

    # Place holder
    nop
  end

end
