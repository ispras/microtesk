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

class BlockRandomTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def pre(self):
        MiniMipsBaseTemplate.pre(self)
        org(0x00020000)
        
    def run(self):
        setattr(thismodule,"instructions",iterate({},
                                            lambda : [
                                                OR(reg(u_()),reg(u_()),reg(u_())),
                                                xor(reg(u_()),reg(u_()),reg(u_())),
                                                AND(reg(u_()),reg(u_()),reg(u_())),
                                                ori(reg(u_()),reg(u_()),1),
                                                xori(reg(u_()),reg(u_()),2),
                                                andi(reg(u_()),reg(u_()),3)
                                                ]
                                            )
                )
            #
    # Constructs 10 instruction sequences which consist of 10 instructions
    # randomly selected from a collection described by the "iterate" construct.
    #
        
        block({'combinator' : 'random'},
              lambda : [
                  instructions.add(10)
                  ]
              ).run(10)
              
              
globals.template = BlockRandomTemplate()
globals.template.generate()
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    