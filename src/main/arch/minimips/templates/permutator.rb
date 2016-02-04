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
# This test template demonstrates how to generate permuations of instructions.
#
class PermutatorTemplate < MiniMipsBaseTemplate

  def run
    # Ordered as is:
    sequence(:permutator => 'trivial') {
      Add reg(_), reg(_), reg(_)
      Sub reg(_), reg(_), reg(_)
      And reg(_), reg(_), reg(_)
      Or  reg(_), reg(_), reg(_)
      Nor reg(_), reg(_), reg(_)
      Xor reg(_), reg(_), reg(_)
    }.run 5

    # Random ordering:
    sequence(:permutator => 'random') {
      # Needed as a place to return from an exception
      epilogue {
        nop
      }

      Add reg(_), reg(_), reg(_)
      Sub reg(_), reg(_), reg(_)
      And reg(_), reg(_), reg(_)
      Or  reg(_), reg(_), reg(_)
      Nor reg(_), reg(_), reg(_)
      Xor reg(_), reg(_), reg(_)
    }.run 5
  end

end
