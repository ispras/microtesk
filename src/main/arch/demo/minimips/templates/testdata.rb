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

require_relative 'minimips_base'

#
# Description:
#
# This test template demonstrates how to use test data generators that are
# independent of the context and produce multiple sets of test data.
#
class TestDataTemplate < MiniMipsBaseTemplate

  def pre
    super

    #
    # Test case level epilogue
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
    # Test data for individual registers.
    sequence(:data_combinator => 'product') {
      add reg(8),  (reg(9)  do testdata('range', :min => 1, :max => 3) end),
                   (reg(10) do testdata('range', :min => 1, :max => 3) end)

      sub reg(11), (reg(12) do testdata('range', :min => 1, :max => 3) end),
                   (reg(13) do testdata('range', :min => 1, :max => 3) end)
    }.run

    # Test data for entire instructions.
    sequence(:data_combinator => 'product') {
      add reg(8),  reg(9),  reg(10) do testdata('range', :min => 1, :max => 3) end
      sub reg(11), reg(12), reg(13) do testdata('range', :min => 1, :max => 3) end
    }.run
  end

end
