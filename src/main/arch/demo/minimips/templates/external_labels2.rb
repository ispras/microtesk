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
# This test template demonstrates how to use external code sections and labels.
# External code is code defined outside blocks describing test cases. It can contain
# labels. These labels are visible from other external code sections and can be used
# to perform control transfers in a backward direction (to earlier generate code).
#
class ExternalLabels2Template < MiniMipsBaseTemplate

  def run
    org 0x00020000

    label :start
    nop
    j :down
    nop

    label :up
    nop

    sequence {
      add reg(_), reg(_), reg(_)
      sub reg(_), reg(_), reg(_)
      nop
    }.run

    j :end
    nop

    org 0x00020100

    label :down
    nop

    j :up
    nop

    label :end
    nop
  end

end
