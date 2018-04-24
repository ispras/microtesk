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
        pass
    
    def at(self,contents):
        pass
    

#template = MiniMipsBaseTemplate()
#template.generate()