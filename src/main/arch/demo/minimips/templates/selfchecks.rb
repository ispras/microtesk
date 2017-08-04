#
# Copyright 2017 ISP RAS (http://www.ispras.ru)
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
# This test template is basic example deonstrates how MicroTESK creates
# self-checking test programs.
#
class SelfChecksTemplate < MiniMipsBaseTemplate

  def initialize
    super
    set_option_value 'self-checks', true
  end

  def pre
    super
    org 0x00020000
  end

  def run
    # A sequence is processed with presimulation enabled
    sequence {
      prepare t0, 2
      prepare t1, 7

      add t2, t1, t0
      sub t3, t1, t0
    }.run

    # A sequence is processed with presimulation disabled
    sequence(:presimulation => false) {
      prepare t0, 2
      prepare t1, 7

      add t2, t1, t0
      sub t3, t1, t0
    }.run
  end

end
