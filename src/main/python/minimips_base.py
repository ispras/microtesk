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

from template import Template

class MiniMipsBaseTemplate(Template):
    def __init__(self):
        Template.__init__(self)
        
        self.set_option_value('indent-token',"\t")
        self.set_option_value('separator-token',"=")
    
    def pre(self):
        
        self.data_config(
            {'target' : 'M'},
            contents = """self.data_manager.define_type({'id' : 'byte', 'text' : '.byte', 'type' : self.data_manager.type('card',8)})""")
        
        self.section_text({'pa' : 0x0, 'va' : 0x0})
        
    
    def post(self):
        pass
    
    def zero(self):
        return self.reg(0)
    
    def at(self):
        return self.reg(1)
    
    def v0(self):
        return self.reg(2)
    
    def v1(self):
        return self.reg(3)
    
    def a0(self):
        return self.reg(4)
    
    def a1(self):
        return self.reg(5)
    
    def a2(self):
        return self.reg(6)
    
    def a3(self):
        return self.reg(7)
    
    def t0(self):
        return self.reg(8)
    
    def t1(self):
        return self.reg(9)
    
    def t2(self):
        return self.reg(10)
    
    def t3(self):
        return self.reg(11)
    
    def t4(self):
        return self.reg(12)
    
    def t5(self):
        return self.reg(13)
    
    def t6(self):
        return self.reg(14)
    
    def t7(self):
        return self.reg(15)
    
    def s0(self):
        return self.reg(16)
    
    def s1(self):
        return self.reg(17)
    
    def s2(self):
        return self.reg(18)
    
    def s3(self):
        return self.reg(19)
    
    def s4(self):
        return self.reg(20)
    
    def s5(self):
        return self.reg(21)
    
    def s6(self):
        return self.reg(22)
    
    def s7(self):
        return self.reg(23)
    
    def t8(self):
        return self.reg(24)
    
    def t9(self):
        return self.reg(25)
    
    def k0(self):
        return self.reg(26)
    
    def k1(self):
        return self.reg(27)
    
    def gp(self):
        return self.reg(28)
    
    def sp(self):
        return self.reg(29)
    
    def fp(self):
        return self.reg(30)
    
    def ra(self):
        return self.reg(31)

#template = MiniMipsBaseTemplate()
#template.generate()
