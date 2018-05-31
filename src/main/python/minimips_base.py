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

from template import reg
from template import Template


class MiniMipsBaseTemplate(Template):
    def __init__(self):
        Template.__init__(self)
        
        self.set_option_value('indent-token',"\t")
        self.set_option_value('separator-token',"=")
        self.set_option_value('default-test-data',False)
    
    def pre(self):
        
        self.data_config(
            {'target' : 'M'},
            lambda : [
                self.data_manager.define_type({'id' : 'byte', 'text' : '.byte', 'type' : self.data_manager.type('card',8)}),
                self.data_manager.define_type({'id' : 'half', 'text' : '.half', 'type' : self.data_manager.type('card',16)}),
                self.data_manager.define_type({'id' : 'word', 'text' : '.word', 'type' : self.data_manager.type('card',32)}),
                self.data_manager.define_space({'id' : 'space', 'text' : '.space', 'fill_with' : 0}),
                self.data_manager.define_ascii_string({'id' : 'ascii', 'text' : '.ascii', 'zero_term' : False}),
                self.data_manager.define_ascii_string({'id' : 'asciiz', 'text' : '.asciiz', 'zero_term' : True}),
                
                ]
            )
        
        self.section_text({'pa' : 0x0, 'va' : 0x0})
        
        self.section_data({'pa' : 0x00080000, 'va' : 0x00080000})
        
    
    def post(self):
        pass
    


def zero():
    return reg(0)

def at():
    return reg(1)

def v0():
    return reg(2)

def v1():
    return reg(3)

def a0():
    return reg(4)

def a1():
    return reg(5)

def a2():
    return reg(6)

def a3():
    return reg(7)

def t0():
    return reg(8)

def t1():
    return reg(9)

def t2():
    return reg(10)

def t3():
    return reg(11)

def t4():
    return reg(12)

def t5():
    return reg(13)

def t6():
    return reg(14)

def t7():
    return reg(15)

def s0():
    return reg(16)

def s1():
    return reg(17)

def s2():
    return reg(18)

def s3():
    return reg(19)

def s4():
    return reg(20)

def s5():
    return reg(21)

def s6():
    return reg(22)

def s7():
    return reg(23)

def t8():
    return reg(24)

def t9():
    return reg(25)

def k0():
    return reg(26)

def k1():
    return reg(27)

def gp():
    return reg(28)

def sp():
    return reg(29)

def fp():
    return reg(30)

def ra():
    return reg(31)
#template = MiniMipsBaseTemplate()
#template.generate()
