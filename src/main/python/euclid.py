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

class EuclidTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def run(self):
        iterate({},
            lambda : [
                add(t0(), t1(), t2()),
                sub(t3(), t4(), t5()),
                add(t7(), t8(), t9())
            ]
        ).run()
        #add(t.t0(), t.zero(), t.zero())
        #addi(t.t1(), t.zero(), 99)
        #add(t.t2(), t.zero(), t.zero())
    def test(self):
        print self
        
globals.template = EuclidTemplate()
globals.template.generate()
        
