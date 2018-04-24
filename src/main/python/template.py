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

from template_builder import define_method_for

class Template:
    template_classes = {}
    
    def __init__(self):
        self.situation_manager = SituationManager(self)
        self.data_manager = None
        
    def template(self):
        return self.template
       
    def pre(self):
        pass
    
    def run(self):
        print "Trying to execute the original Template"

    def post(self):
        pass
    
    def data_config(self,attrs,**kwarg):
        if None != self.data_manager:
            raise NameError('Data configuration is already defined')
        
        target = attrs.get('target')
        contents = kwarg['contents']
        
        # Default value is 8 bits if other value is not explicitly specified
        
        if 'item_zise' in attrs:
            addressableSize = attrs.get('item_size')
        else:
            addressableSize = 8
        manager = self.template.getDataManager()
        print "manager is {}".format(manager)
        self.data_manager = DataManager(self, self.template.getDataManager())
        self.data_manager.beginConfig(target,addressableSize)
        
        exec(contents)
        self.data_manager.endConfig()
        
        
        

# -------------------------------------------------------------------------- #
# Sections                                                                   #
# -------------------------------------------------------------------------- #
    def section(self,attrs):
        from java.math import BigInteger        

        name = attrs.get('name')
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        self.template.beginSection(name,pa,va,args)
        #self.eval(contents)
        self.template.endSection()
        
    
    def section_text(self,attrs):
        from java.math import BigInteger
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        
        self.template.beginSectionText(pa,va,args)
        #self.eval(contents)
        self.template.endSection()
        
    def section_data(self,attrs):
        from java.math import BigInteger
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        self.template.beginSectionData(pa,va,args)
        #self.eval(contents)
        self.template.endSection()
    
    def generate(self):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        
        self.template = engine.newTemplate()
        #TemplateBuilder.define_runtime_methods(engine.getModel.getMetaData)
        
        self.template.beginPreSection()
        self.pre()
        self.template.endPreSection()
        
        
        self.template.beginPostSection()
        self.post()
        self.template.endPostSection()
        
        
        self.template.beginMainSection()
        self.run()
        self.template.endMainSection()
        
        
    def set_option_value(self,name,value):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        engine.setOptionValue(name, value)  
        
    def get_option_value(self,name):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        engine.getOptionValue(name)
        
    def rev_id(self):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        engine.getModel.getRevisionId()
        
    def is_rev(self,id):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        engine.isRevision(id)

class SituationManager:
    def __init__(self,template):
        self.template = template
        
class DataManager:
    
    class Type:
        def __init__(self,*args):
            self.name = args[0]
            self.args = [args[i] for i in range(1,len(args))] if len(args) > 1 else []
            
        def name(self):
            return self.name
        
        def args(self):
            return self.args
        
    def __init__(self,template,manager):
        self.template = template
        self.manager = manager
        
        self.builder = None
        self.ref_count = 0
        
    def beginConfig(self,target,addressableSize):
        self.configurer = self.manager.beginConfig(target,addressableSize)
    
    def endConfig(self):
        self.manager.endConfig()
        self.configurer = None
        
    def beginData(self,global1,separate_file):
        if self.ref_count == 0:
            self.builder = self.template.template.beginData(global1,separate_file)
        
        self.ref_count = self.ref_count + 1
        return self.builder
    
    def endData(self):
        self.ref_count  = self.ref_count - 1
        if self.ref_count == 0:
            self.template.template.endData()
            self.builder = None
    
    def align(self,value):
        value_in_bytes = self.template.alignment_in_bytes(value)
        self.builder.align(value,value_in_bytes)
    
    def org(self,origin):
        if type(origin) is int:
            self.builder.setOrigin(origin)
        elif type(origin) is dict:
            delta = origin.get('delta')
            if type(delta) is not int:
                raise TypeError("delta must be int")
            self.builder.setRelativeOrigin(delta)
        else:
            raise TypeError("origin must be int or dict")
            
    def type(self,*args):
        
        return DataManager.Type(*args)
    
    def label(self,id):
        self.builder.addLabel(id,False)
    
    def global_label(self,id):
        self.builder.addLabel(id,True)
    
    def rand(self,from1,to):
        self.template.rand(from1,to)
        
    def dist(self,*ranges):
        self.template.dist(*ranges)
    
    def range(self,attrs):
        self.template.range(attrs)
    
    def define_type(self,attrs):
        id = attrs.get('id')
        text = attrs.get('text')
        type = attrs.get('type')
        
        self.configurer.defineType(id,text,type.name,type.args)
        
        def p(*arguments):
            dataBuilder = self.builder.addDataValues(id)
            for x in arguments:
                dataBuilder.add(x)
            dataBuilder.build()
        
        define_method_for(DataManager,id,'type',p)
        
    def define_space(self,attrs):
        id = attrs.get('id')
        text = attrs.get('text')
        fillWith = attrs.get('fill_with')
        
        self.configurer.defineSpace(id,text,fillWith)
        
        def p(length):
            self.builder.addSpace(length)
        
        define_method_for(DataManager,id,'space',p)
        
    def define_ascii_string(self,attrs):
        id = attrs.get('id')
        text = attrs.get('text')
        zeroTerm = attrs.get('zero_term')
        
        self.configurer.defineAsciiString(id,text,zeroTerm)
        
        def p(*strings):
            self.builder.addAsciiStrings(zeroTerm,strings)
        
        define_method_for(DataManager,id,'string',p)
        
    def text(self,value):
        self.builder.addText(value)
        
    def comment(self,value):
        self.builder.addComment(value)
        
    def value(self,*args):
        self.template.value(*args)
        
    def data(self,contents):
        exec(contents)
        
        
        
        
        
        
        
        
        
        
        
        
            
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
        
        if type(attrs) is dict:
            try:
                self.preparator
            except NameError:
                print "page_table_preparator is not defined."
            
            prep = self.preparator
            entry = Entry(attrs)
            exec(prep)
        elif type(attrs) is MemoryObject:
            try:
                self.adapter
            except NameError:
                print "page_table_adapter is not defined"
            exec(self.adapter)
        
    class Entry:
        def __init__(self,attrs):  
            if type(attrs) is not dict:
                raise TypeError("attrs must be dict")
            self.attrs = attrs
            
        
        
        
        
        
        
        
        
        
        
        
        
        
             
     
     
     
     
     
     
     
     
     
     
     
     
     
        