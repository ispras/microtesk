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

class BubbleSortTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def pre(self):
        MiniMipsBaseTemplate.pre(self)
        
        data({},
                  lambda : [
                      self.data_manager.label('data'),
                      self.data_manager.word(rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)),
                      self.data_manager.word(rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)),
                      self.data_manager.word(rand(1, 9), rand(1, 9), rand(1, 9), rand(1, 9)),
                      self.data_manager.label('end'),
                      self.data_manager.space(1)
                      ]
                  )
        
    def run(self):
        def swap(reg1,reg2):
            xor(reg1,reg1,reg2)
            xor(reg2,reg1,reg2)
            xor(reg1,reg1,reg2)
        
        sequence({},
                 lambda : [
                      trace_data('data', 'end'),

                      la(s0(), 'data'),
                      la(s1(), 'end'),
                
                      add(t0(), zero(), zero()),
                      ########################### Outer loop starts ##############################
                      label('repeat'),
                
                      addi(t1(), s0(), 4),
                      ########################### Inner loop starts ##############################
                      label('for'),
                      beq(t1(), s1(), 'exit_for'),
                
                      addi(t3(), zero(), 4),
                      sub(t2(), t1(), t3()),
                
                      lw(t4(), 0, t1()),
                      lw(t5(), 0, t2()),
                
                      slt(t6(), t4(), t5()),
                      beq(t6(), zero(), 'next'),
                      nop(),
                
                      swap(t4(), t5()),
                      addi(t0(), zero(), 1),
                
                      sw(t4(), 0, t1()),
                      sw(t5(), 0, t2()),
                
                      label('next'),
                      j('for'),
                      addi(t1(), t1(), 4),
                      ############################ Inner loop ends ###############################
                      label('exit_for'),
                
                      bne(t0(), zero(), 'repeat'),
                      add(t0(), zero(), zero()),
                      ############################ Outer loop ends ###############################
                
                      trace_data('data', 'end')
                     ]
                 ).run()
        
        
        
globals.template = BubbleSortTemplate()
globals.template.generate()
        
        
        
        
        
        
        
        
        
        
        
        
