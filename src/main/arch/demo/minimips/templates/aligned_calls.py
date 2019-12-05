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
import globals
from minimips_base import MiniMipsBaseTemplate
from minimips_base import *
from template import *

class AlignedCallsTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def pre(self):
        MiniMipsBaseTemplate.pre(self)
     
    def run(self):
        align(4)
        add(reg(u_()), get_register(), get_register())
        sub(reg(u_()), get_register(), get_register())
        nop()
    
        align(8)
        add(reg(u_()), get_register(), get_register())
        sub(reg(u_()), get_register(), get_register())
        nop()
    
        align(16)
        add(reg(u_()), get_register(), get_register())
        sub(reg(u_()), get_register(), get_register())
        nop()
        
globals.template = AlignedCallsTemplate()
globals.template.generate()
