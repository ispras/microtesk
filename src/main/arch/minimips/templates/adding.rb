#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

class  AddingTemplate < MinimipsBaseTemplate

    def run
        addi reg(1), reg(0), 1
        addi reg(2), reg(0), 2
        add reg(3), reg(2), reg(1)

        trace "\nValue in register $3 = %d\n", gpr(3)
    end
end