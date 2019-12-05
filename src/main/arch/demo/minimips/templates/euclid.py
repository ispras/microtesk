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

class EuclidTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def run(self):
        sequence({},
            lambda : [
                setattr(thismodule,"val1",rand(1,63)),
                setattr(thismodule,"val2",rand(1,63)),
                
                trace("\nInput parameter values: %d, %d\n", val1, val2),

                prepare(t1(), val1),
                prepare(t2(), val2),
                
                label('cycle'),
                trace("\nCurrent values: $t1($9)=%d, $t2($10)=%d\n", gpr(9), gpr(10)),
                beq(t1(), t2(), 'done'),
                
                slt(t0(), t1(), t2()),
                bne(t0(), zero(), 'if_less'),
                nop(),
                
                subu(t1(), t1(), t2()),
                j('cycle'),
                nop(),
                
                label('if_less'),
                subu(t2(), t2(), t1()),
                j('cycle'),
                
                label('done'),
                add(t3(), t1(), zero()),
                
                trace("\nResult stored in $t3($11): %d", gpr(11))
                
            ]
        ).run()
        
globals.template = EuclidTemplate()
globals.template.generate()
        
