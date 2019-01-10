#
# Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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
# This test template demonstrates how to randomly allocate registers so
# that they do not conflict with other registers used in the test case.
#
class RegisterAllocationTemplate < MiniMipsBaseTemplate

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
    # Destination of all instructions is a random register that
    # is not used in this sequence.
    sequence {
      # Randomly selects destination registers from free registers
      add reg(_ TRY_FREE, :retain  => [t0, t1, t2, t3]), t4, t5
      sub reg(_ TRY_FREE, :rate    => {:read => 50, :write => 50}), t6, t7
      slt reg(_ TRY_FREE, :exclude => [ zero, at, k0, k1, gp, sp, fp, ra ]), t8, t9
    }.run 3
  end

end
