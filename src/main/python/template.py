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

import template_builder as TemplateBuiler

class Template:
    template_classes = {}
    
    def __init__(self):
        self.situation_manager = SituationManager(self)
        
    def pre(self):
        pass
    
    def run(self):
        print "Trying to execute the original Template"

    def post(self):
        pass
    
    def data_config(self,attrs):
        pass
    
# -------------------------------------------------------------------------- #
# Sections                                                                   #
# -------------------------------------------------------------------------- #
    def section(self,attrs):
        from java.math import BigInteger        

        name = attrs['name']
        
        pa = attrs['pa']
        va = attrs['va']
        args = attrs['args']
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        self.template.beginSection(name,pa,va,args)
        #self.eval(contents)
        self.template.endSection
        
    
    def section_text(self,attrs):
        from java.math import BigInteger
        
        pa = attrs['pa']
        va = attrs['va']
        args = attrs['args']
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        self.template.beginSectionText(pa,va,args)
        #self.eval(contents)
        self.template.endSection
        
    def section_data(self,attrs):
        from java.math import BigInteger
        
        pa = attrs['pa']
        va = attrs['va']
        args = attrs['args']
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        self.template.beginSectionData(pa,va,args)
        #self.eval(contents)
        self.template.endSection
    
    def generate(self):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        
        self.template = engine.newTemplate()
        #TemplateBuilder.define_runtime_methods(engine.getModel.getMetaData)
        
        self.template.beginPreSection()
        self.pre()
        self.template.endPreSection()
        
        
        self.template.beginPostSection
        self.post()
        self.template.endPostSection
        
        
        self.template.beginMainSection
        self.run()
        self.template.endMainSection
              

class SituationManager:
    def __init__(self,template):
        self.template = template
        
class WrappedObject:
    def java_object(self):
        raise NotImplementedError('Method java_object is not implemented')
        
class AddressReference(WrappedObject):
    def __init__(self):
        WrappedObject.__init__(self)
        self.template = template
        self.level  = 0
    
    def box(self,arg):
        self.level = arg
        return self
    
    def java_object(self):
        template.newAddressReference(self.level)
    
    def call(self,min,max):
        bits(min,max)
    
    def bits(self,min,max):
        self.template.newAddressReference(self.level,min,max)
        
class BufferEntryReference(WrappedObject):
    def _init__(self):
        WrappedObject.__init__(self)
        self.template = template
        self.level = 0
        
    def box(self,arg):
        self.level = arg
        return self
    
    def java_object(self):
        self.template.newEntryReference(self.level)
    
    def call(self,min,max):
        bits(min,max)
    
    def bits(self,min,max):
        self.template.newEntryReference(self.level,min,max)
        
        
class PageTable:
    def __init__(self,template,data_manager):
        self.template = template
        self.data_manager = data_manager
        
    def text(self,value):
        self.data_manager.text(value)
     
    def page_table_preparator(self,contents):
        self.preparator = contents
    
    def page_table_adaptor(self,contents):
        self.adapter = contents
    
    def org(self,address):
        self.data_manager.org(address)
    
    def align(self,value):
        self.data_manager.align(value)
        
    def label(self,id):
        self.data_manager.label(id)
    
    def global_label(self,id):
        self.data_manager.global_label(id)
        
    def memory_object(self,attrs):
        self.template.memory_object(attrs)
        
    def page_table_entry(self,attrs):
        from java.ru.ispras.microtesk.test.template import MemoryObject
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
             
     
     
     
     
     
     
     
     
     
     
     
     
     
        