#
# Copyright 2016 ISP RAS (http://www.ispras.ru)
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
      add reg1=get_register, t0, t1
      sub reg2=get_register, t2, t3
      slt reg3=get_register, t4, t5
      newline

      # Frees the previously reserved registers
      free_register reg1
      free_register reg2
      free_register reg3

      # Randomly selects destination registers from free registers including
      # those that were previosly freed
      add get_register(:exclude => [at, v0, v1]), s0, s1
      sub get_register(:exclude => [at, v0, v1]), s2, s3
      slt get_register(:exclude => [at, v0, v1]), s4, s5
      newline
    }.run 3
  end
end
