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

class Template:
    template_classes = {}
    
    def __init__(self):
        self.situation_manager = SituationManager(self)
    
    #@classmethod
    #def inherited(cls,subclass):
        #from inspect import getframeinfo, stack
        #caller = getframeinfo(stack()[1][0])
        #subclass_file = caller.filename
        #print "Loaded template {subclass} defined in {subclass_file}".format(subclass,subclass_file)
        #template_classes[subclass] = subclass_file
        
    def pre(self):
        pass
    
    def run(self):
        print "Trying to execute the original Template"
    def post(self):
        pass
    
    def generate(self):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        
        template = engine.newTemplate()
        
        template.beginPreSection()
        
        template.endPreSection()
class SituationManager:
    def __init__(self,template):
        self.template = template