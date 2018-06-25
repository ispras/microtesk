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
import sys

thismodule = sys.modules[__name__]

class ConstraintControlFlowTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def pre(self):
        MiniMipsBaseTemplate.pre(self)
        org(0x00020000)
        
    def run(self):
        sequence({},
                 lambda : [
                            prepare(t0(), rand(0, 1)),
                            prepare(t1(), rand(0, 1)),
                            
                            label('add_start'),
                            bne(t0(), zero(), 'add_overflow'),
                            nop(),
                            
                            add(t2(), t3(), t4(),{'situations' : lambda : [situation('normal')]}),
                            j('sub_start'),
                            nop(),
                            
                            label('add_overflow'),
                            add(t2(), t3(), t4(),{'situations' : lambda : [situation('IntegerOverflow')]}),
                            
                            label('sub_start'),
                            bne(t1(), zero(), 'sub_overflow'),
                            nop(),
                            
                            sub(t5(), t6(), t7(),{'situations' : lambda : [situation('normal')]}),
                            j('exit'),
                            nop(),
                            
                            label('sub_overflow'),
                            sub(t5(), t6(), t7(),{'situations' : lambda : [situation('IntegerOverflow')]}),
                            
                            label('exit'),
                            nop()
                     ]
                 ).run(10)
                 
                 
                 
globals.template = ConstraintControlFlowTemplate()
globals.template.generate()