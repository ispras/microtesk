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

import template_builder
import globals



class Template:
    template_classes = {}
    
    def __init__(self):
        self.situation_manager = SituationManager(self)
        self.data_manager = None
        self.default_mode_allocator = None
         
    #def template(self):
     #   return self.tempset_default_mode_allocatorlate
    
    def get_caller_location(self,caller_index = 1):
        #import inspect
        #(frame, filename, line_number, 
         #function_name, lines, index) = inspect.getouterframes(inspect.currentframe())[2]
        #return self.template.where(filename,line_number)
        return self.template.where("xxx.py",1)
    
    def define_method(self,method_name,method_body):
        method_name = method_name.lower()
        if not hasattr(Template, method_name):
            setattr(Template,method_name,MethodType(method_body,None,Template))
        else:
            print "Error: Failed to define the {} method.".format(method_name)
            
  # ------------------------------------------------------------------------- #
  # Main template writing methods                                             #
  # ------------------------------------------------------------------------- #

  # Pre-condition instructions template     
    def pre(self):
        pass
    
  # Main instructions template    
    def run(self):
        print "Trying to execute the original Template"
        
  # Post-condition instructions template
    def post(self):
        pass
    
    def executed(self,contents = lambda : []):
        return self.set_attributes({'executed' : True}, contents)
        
    def nonexecuted(self,contents = lambda : []):
        return self.set_attributes({'executed' : False}, contents)
    
    def branches(self,contents = lambda : []):
        return self.set_attributes({'branches' : True}, contents)
    
    def set_attributes(self,attributes,contents):
        mapBuilder = set_builder_attributes(self.template.newMapBuilder, attributes)
        self.template.beginAttributes(mapBuilder)
        contents()
        self.template.endAttributes()
        
    
    def testdata(name, attrs = {}):
        get_new_situation(name, attrs, True)
        
    def situation(name, attrs = {}):
        get_new_situation(name, attrs, False)
        
    def get_new_situation(name, attrs, testdata_provider):
        if not isinstance(attrs,dict):
            raise TypeError('attrs must be dict')
        
        builder = self.template.newSituation(name, testdata_provider)
        
        for name,value in attrs.iteritems():
            if isintance(value,Dist):
                attr_value = value.java_object
            else:
                attr_value = value
           
            builder.setAttribute(name,attr_value)
            
        return builder.build()
    
    def random_situation(dist):
        return dist.java_object
    
    def set_default_situation(names, situations = lambda : []):
        if not isintance(names,basestring) and not isintance(name,list):
            raise TypeError("names must be String or List.")


        default_situation = self.situation_manager.situations()
        if isinstance(names,list):
            for name in names:
                self.template.setDefaultSituation(name, default_situation)
        else:
            self.template.setDefaultSituation(names, default_situation)
            
                
        
  # -------------------------------------------------------------------------- #
  # Data Definition Facilities                                                 #
  # -------------------------------------------------------------------------- #
    
    def data_config(self,attrs,contents = lambda : []):
        if None != self.data_manager:
            raise NameError('Data configuration is already defined')
        
        target = attrs.get('target')
        
        # Default value is 8 bits if other value is not explicitly specified
        
        if 'item_zise' in attrs:
            addressableSize = attrs.get('item_size')
        else:
            addressableSize = 8
        self.data_manager = DataManager(self, self.template.getDataManager())
        self.data_manager.beginConfig(target,addressableSize)
        contents()
        return self.data_manager.endConfig()
    
    def data(self,attrs = {},contents = lambda : []):
        if self.data_manager is None:
            raise NameError('Data configuration is not defined')
        
        Global = attrs.get('global')
        if Global is None:
            Global = False
            
        separate_file = attrs.get('separate_file')
        if separate_file is None:
            separate_file = False
        self.data_manager.beginData(Global,separate_file)
        contents()
        return self.data_manager.endData()
    
                
            
        
        

# -------------------------------------------------------------------------- #
# Sections                                                                   #
# -------------------------------------------------------------------------- #
    def section(self,attrs,contents = lambda : []):
        from java.math import BigInteger        

        name = attrs.get('name')
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        self.template.beginSection(name,pa,va,args)
        contents()
        self.template.endSection()
        
    
    def section_text(self,attrs,contents = lambda : []):
        from java.math import BigInteger
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        
        self.template.beginSectionText(pa,va,args)
        contents()
        self.template.endSection()
        
    def section_data(self,attrs,contents = lambda : []):
        from java.math import BigInteger
        
        pa = attrs.get('pa')
        va = attrs.get('va')
        args = attrs.get('args')
        
        pa = BigInteger(str(pa))
        va = BigInteger(str(va))
        
        
        
        self.template.beginSectionData(pa,va,args)
        contents()
        self.template.endSection()
    
    def generate(self):
        from ru.ispras.microtesk.test import TestEngine
        engine = TestEngine.getInstance()
        
        
        self.template = engine.newTemplate()
        #define_runtime_methods(engine.getModel().getMetaData())
        
        self.template.beginPreSection()
        self.pre()
        self.template.endPreSection()
        
        
        self.template.beginPostSection()
        self.post()
        self.template.endPostSection()
        
        
        self.template.beginMainSection()
        self.run()
        self.template.endMainSection()
        
        
        
    def set_builder_attributes(self,builder,attributes):
        for key in attributes:
            value = attributes[key]
            if isinstance(value, dict):
                mapBuilder = set_builder_attributes(self.template.newMapBuilder(), value)
                builder.setAttribute(key, mapBuilder.getMap())
            else:
                builder.setAttribute(key,value)
                
        return builder
    
    def numeric_label_ref(self,index,forward):
        if not isinstance(index,it):
            raise TypeError('index is not integer')
        if not index in range(0,10):
            raise TypeError('index should be within the range 0..9')
        
        return self.template.newNumericLabelRef(index,forward)
            

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
        from java.math import BigInteger
        value_in_bytes = alignment_in_bytes(value)
        return self.builder.align(BigInteger(str(value)),BigInteger(str(value_in_bytes)))
    
    def org(self,origin):
        from java.math import BigInteger
        if type(origin) is int:
            self.builder.setOrigin(origin)
        elif type(origin) is dict:
            delta = origin.get('delta')
            if type(delta) is not int:
                raise TypeError("delta must be int")
            self.builder.setRelativeOrigin(BigInteger(str(delta)))
        else:
            raise TypeError("origin must be int or dict")
            
    def type(self,*args):
        return DataManager.Type(*args)
    
    def label(self,id):
        self.builder.addLabel(id,False)
    
    def global_label(self,id):
        self.builder.addLabel(id,True)
    
    def rand(self,from1,to):
        return self.template.rand(from1,to)
        
    def dist(self,*ranges):
        return self.template.dist(*ranges)
    
    def range(self,attrs):
        return self.template.range(attrs)
    
    def define_type(self,attrs):
        id = attrs.get('id')
        text = attrs.get('text')
        type = attrs.get('type')
        
        self.configurer.defineType(id,text,type.name,type.args)
        
        print "in define_type it's {}".format(self)
        
        def p(self,*arguments):
            dataBuilder = self.builder.addDataValues(id)
            for x in arguments:
                print "x is {}".format(x)
                dataBuilder.add(x)
            dataBuilder.build()
        
        template_builder.define_method_for(DataManager,id,'type',p)
        
    def define_space(self,attrs):
        from java.math import BigInteger
        id = attrs.get('id')
        text = attrs.get('text')
        fillWith = attrs.get('fill_with')
        
        self.configurer.defineSpace(id,text,BigInteger(str(fillWith)))
        
        def p(self,length):
            self.builder.addSpace(length)
        
        template_builder.define_method_for(DataManager,id,'space',p)
        
    def define_ascii_string(self,attrs):
        id = attrs.get('id')
        text = attrs.get('text')
        zeroTerm = attrs.get('zero_term')
        
        self.configurer.defineAsciiString(id,text,zeroTerm)
        
        def p(self,*strings):
            self.builder.addAsciiStrings(zeroTerm,strings)
        
        template_builder.define_method_for(DataManager,id,'string',p)
        
    def text(self,value):
        return self.builder.addText(value)
        
    def comment(self,value):
        return self.builder.addComment(value)
        
    #def value(self,*args):
      #  return self.template.value(*args)
        
    def data(self,contents = lambda : []):
        contents()
        
    def evaluate(self,contents):
        contents()    
        
        
        
        
        
        
        
        
        
        
            
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
        return template.newAddressReference(self.level)
    
    def call(self,min,max):
        return bits(min,max)
    
    def bits(self,min,max):
        return self.template.newAddressReference(self.level,min,max)
        
class BufferEntryReference(WrappedObject):
    def _init__(self):
        WrappedObject.__init__(self)
        self.template = template
        self.level = 0
        
    def box(self,arg):
        self.level = arg
        return self
    
    def java_object(self):
        return self.template.newEntryReference(self.level)
    
    def call(self,min,max):
        return bits(min,max)
    
    def bits(self,min,max):
        return self.template.newEntryReference(self.level,min,max)
        
        
class PageTable:
    def __init__(self,template,data_manager):
        self.template = template
        self.data_manager = data_manager
        
    def text(self,value):
        return self.data_manager.text(value)
     
    def page_table_preparator(self,contents = lambda : []):
        self.preparator = contents
        return self.preparator
    
    def page_table_adaptor(self,contents = lambda : []):
        self.adapter = contents
        return self.adapter
    
    def org(self,address):
        return self.data_manager.org(address)
    
    def align(self,value):
        return self.data_manager.align(value)
        
    def label(self,id):
        return self.data_manager.label(id)
    
    def global_label(self,id):
        return self.data_manager.global_label(id)
        
    def memory_object(self,attrs):
        return self.template.memory_object(attrs)
        
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
            
            
class Dist:
    def __init__(self,java_object):
        self.java_object = java_object
    
    def java_object(self):
        return self.java_object
        
    

    def next_value(self):
        return self.java_object.value()
    
class ValueRange:
    def __intit__(self,value, bias):
        self.value = value
        self.bias = bias
        
class Location:
    def __init__(self,name, index):
        self.name = name
        self.index = index
        
class RangeClass:
    def __init__(self,min,max):
        self.min = min
        self.max = max
        
    

        
        


    

            
            
def sequence(attributes,contents = lambda : []):
    blockBuilder = globals.template.template.beginBlock()
    blockBuilder.setWhere(globals.template.get_caller_location())

    blockBuilder.setAtomic(False)
    blockBuilder.setSequence(True)
    blockBuilder.setIterate(False)

    if 'obfuscator' in attributes:
      blockBuilder.setObfuscator(attributes['obfuscator'])

    globals.template.set_builder_attributes(blockBuilder, attributes)
    contents()

    return globals.template.template.endBlock()

def atomic(attributes,contents = lambda : []):
    blockBuilder = globals.template.template.beginBlock()
    blockBuilder.setWhere(globals.template.get_caller_location())

    blockBuilder.setAtomic(True)
    blockBuilder.setSequence(False)
    blockBuilder.setIterate(False)

    if 'obfuscator' in attributes:
      blockBuilder.setObfuscator(attributes['obfuscator'])

    globals.template.set_builder_attributes(blockBuilder, attributes)
    contents()

    return globals.template.template.endBlock()
    
def iterate(attributes,contents = lambda : []):
    blockBuilder = globals.template.template.beginBlock()
    blockBuilder.setWhere(globals.template.get_caller_location())

    blockBuilder.setAtomic(False)
    blockBuilder.setSequence(False)
    blockBuilder.setIterate(True)

    if 'obfuscator' in attributes:
      blockBuilder.setObfuscator(attributes['obfuscator'])
      
    if 'rearranger' in attributes:
      blockBuilder.setRearranger(attributes['rearranger'])

    globals.template.set_builder_attributes(blockBuilder, attributes)
    contents()

    return globals.template.template.endBlock()

def block(attributes,contents = lambda : []):
    blockBuilder = globals.template.template.beginBlock()
    blockBuilder.setWhere(globals.template.get_caller_location())

    blockBuilder.setAtomic(False)
    blockBuilder.setSequence(False)
    blockBuilder.setIterate(False)
    
    if 'combinator' in attributes:
        blockBuilder.setCombinator(attributes['combinator'])

    if 'permutator' in attributes:
      blockBuilder.setPermutator(attributes['permutator'])

    if 'compositor' in attributes:
      blockBuilder.setCompositor(attributes['compositor'])

    if 'rearranger' in attributes:
      blockBuilder.setRearranger(attributes['rearranger'])

    if 'obfuscator' in attributes:
      blockBuilder.setObfuscator(attributes['obfuscator'])

    globals.template.set_builder_attributes(blockBuilder, attributes)
    contents()
    
    return globals.template.template.endBlock()

def rand(*args):
    from java.math import BigInteger
    if len(args) is 1:
        distribution = args[0]
        
        if not isinstance(distribution,Dist):
            raise TypeError('argument must be a distribution')
        
        return globals.template.template.newRandom(distribution.java_object)
    elif len(args) is 2:
        From = args[0]
        To = args[1]
        
        if not isinstance(From,int) or not isinstance(To,int):
            raise TypeError('arguments must be integers')
        
        return globals.template.template.newRandom(BigInteger(str(From)),BigInteger(str(To)))
    else:
        raise TypeError('wrong argument count')
        
        
def dist(*ranges):
    if not isintance(ranges,list):
        raise TypeError('ranges is not list')
    
    builder = globals.template.template.newVariateBuilder()
    for range_item in ranges:
        if not isinstance(range_item, ValueRange):
            raise TypeError('range_item is not ValueRange')
        
        value = range_items.value
        bias = range_items.bias
        
        if isintance(value,list):
            if bias is None:
                builder.addCollection(value)
            else:
                builder.addCollection(value,bias)
        elif isinstance(value,Dist):
            if bias is None:
                builder.addVariate(value.java_object)
            else:
                builder.addVariate(value.java_object,bias)
        else:
            if bias is None:
                builder.addValue(value)
            else:
                builder.addValuer(value,bias)
        
    
    return Dist(builder.build())

def Range(attrs = {}):
    if not isinstance(attrs,dict):
        raise TypeError("attrs is not dict")
    
    value = attrs.get('value')
    
    bias = None
    
    bias = attrs.get('bias')
    
    return ValueRange(value,bias)
            
        
def u_(allocator = None,attrs = {}):
    if isinstance(allocator,dict) and not attrs:
        attrs = allocator
        allocator = None
    
    if not isinstance(attrs,dict):
        raise TypeErro('attrs should be dict')
    
    retain = attrs.get('retain')
    exclude = attrs.get('exclude')
    
    if allocator is None:
        allocator = globals.template.default_mode_allocator
        
    return globals.template.template.newUnknownImmediate(globals.template.get_caller_location(),allocator,retain,exclude)
    
def u_label():           
        return globals.template.template.newLazyLabel()
    
def mode_allocator(name,attrs = {}):
    builder = globals.template.template.newAllocatorBuilder(name)
    
    for key,value in attrs.iteritems():
        builder.setAttribute(key,value)
        
    return builder.build()

def set_default_mode_allocator(allocator):
    globals.template.default_mode_allocator = allocator
    
def free_allocated_mode(mode):
    return globals.template.template.freeAllocatedMode(mode,False)
        
def free_all_allocated_mode(mode):
    return globals.template.template.freeAllocatedMode(mode,True)

def define_mode_group(name, distribution):
    if not isintance(distribution,Dist):
      raise TypeError("distribution is not a Dist.")
    
    globals.template.template.defineGroup(name, distribution.java_object)
    template_builder.define_addressing_mode_group(name)

def define_op_group(name, distribution):
    if not isinstance(distribution,Dist):
        raise TyperError("distribution is not a Dist.")
    
    globals.template.template.defineGroup(name, distribution.java_object)
    template_builder.define_operation_group(name)
  
# -------------------------------------------------------------------------- #
# Printing Text Messages                                                     #
# -------------------------------------------------------------------------- #

#
# Creates a location-based format argument for format-like output methods.
#
def location(name, index):
    return Location(name,index)

#
# Prints text into the simulator execution log.
#
def trace(format, *args):
    return print_format('TRACE', format, *args)

#
# Adds the new line character into the test program
#
def newline():
    return text('')

#
# Adds text into the test program.
#
def text(format, *args):
    if globals.is_multiline_comment:
        print_format('COMMENT_ML_BODY', format, *args)
    else:
        print_format('TEXT', format, *args)


#
# Adds a comment into the test program (uses sl_comment_starts_with).
#
def comment(format, *args):
    print_format('COMMENT', format, *args)


#
# Starts a multi-line comment (uses sl_comment_starts_with)
#
def start_comment():
    globals.is_multiline_comment = True
    print_format('COMMENT_ML_START', '')

#
# Ends a multi-line comment (uses ml_comment_ends_with)
#
def end_comment():
    print_format('COMMENT_ML_END', '')
    globals.is_multiline_comment = False

#
# Prints a format-based output to the simulator log or to the test program
# depending of the is_runtime flag.
#
def print_format(kind, format, *args):
    import ru.ispras.microtesk.test.template.Value as Value
    from java.math import BigInteger
    
    builder = globals.template.template.newOutput(kind, format)
    
    for arg in args:
      if isinstance(arg,int) or isinstance(arg,basestring) or isinstance(arg,Value):
        builder.addArgument(arg)
      elif isinstance(arg,Location):
        builder.addArgument(arg.name, BigInteger(str(arg.index)))
      else:
        raise TypeError("Illegal format argument class")
    
    return globals.template.template.addOutput(builder.build())

#
# Creates a pseudo instruction call that prints user-specified text.
#
def pseudo(text):
    globals.template.template.setCallText(text)
    globals.template.template.endBuildingCall()

def preparator(attrs,contents = lambda : []):
    return create_preparator(False,attrs,contents)

def comparator(attrs,contents = lambda : []):
    return create_preparator(True,attrs,contents)

def create_preparator(is_comparator,attrs,contents = lambda : []):
    from java.math import BigInteger
    target = attrs.get('target')
    
    builder = globals.template.template.beginPreparator(target,is_comparator)
    builder.setWhere(globals.template.get_caller_location())
    
    name = attrs.get('name')
    
    if name is not None:
        builder.setName(name)
        
    mask = attrs.get('mask')
    if mask is not None:
        if isinstance(mask,basestring):
            builder.setMaskValue(mask)
        elif isintance(mask,list):
            builder.setMaskCollection(mask)
        else:
            raise TypeError("Illegal mask type")
        
    arguments = attrs.get('arguments')
    if arguments is not None:
        if not isinstance(arguments,dict):
            raise TypeError("arguments is not dict")
        
        for name,value in arguments.iteritems():
            if isinstance(value,int):
                builder.addArgumentValue(name,BigInteger(str(value)))
            elif isinstance(value,RangeClass):
                builder.addArgumentRange(name,BigInteger(str(value.min)),BigInteger(str(value.max)))
            elif isinstance(value,list):
                builder.addArgumentCollection(name,value)
            else:
                raise TypeError("Illegal value of argument")
            
    contents()
    globals.template.template.endPreparator()

def variant(attrs = {},contents = lambda : []):
    name = attrs.get('name')
    bias = attrs.get('bias')
    from java.math import BigInteger
    if bias is not None:
        x = BigInteger(str(bias))
    else:
        x = bias
    globals.template.template.beginPreparatorVariant(name,x)
    contents()
    globals.template.template.endPreparatorVariant()
    
def target():
    return globals.template.template.getPreparatorTarget()

def value(*args):
    if len(args) is not 0 and len(args) is not 2:
        raise TypeError("wrong argument count, should be 0 or 2")
    
    if len(args) is 2:
        return globals.template.template.newLazy(args[0],args[1])
    else:
        x = globals.template.template.newLazy()
        print "new lazy is {}".format(x)
        return x
        
def sign_extend(value_object,bit_size):
    if isinstance(value_object,WrappedObject):
        value_object = value_object.java_object
    return value_object.signExtend(bit_size)

def zero_extend(value_object,bit_size):
    if isinstance(value_object,WrappedObject):
        value_object = value_object.java_object
    return value_object.zeronExtend(bit_size)

def prepare(target_mode,value_object,attrs = {}):
    preparator_name = attrs.get('name')
    variant_name = attrs.get('variant')
    
    if isinstance(value_object,WrappedObject):
        value_object = value_object.java_object
        
    return globals.template.template.addPreparatorCall(target_mode,value_object,preparator_name,variant_name)
    

def org(origin):
    from java.math import BigInteger
    if isinstance(origin,int):
        globals.template.template.setOrigin(BigInteger(str(origin)),globals.template.get_caller_location())
    elif isinstance(origin,dict):
        delta = origin.get('delta')
        if not isinstance(delta,int):
            raise TyperError('delta should be integer')
        globals.template.template.setRelativeOrigin(BigInteger(str(delta)),globals.template.get_caller_location())
    else:
        raise TypeError('origin should be integer or dict')

def align(value):
    from java.math import BigInteger
    value_in_bytes = alignment_in_bytes(value)
    value_in_bytes = BigInteger(str(value_in_bytes))
    globals.template.template.setAlignment(BigInteger(str(value)),value_in_bytes,globals.template.get_caller_location())

def alignment_in_bytes(n):
    return 2**n    


def label(name):
    if isinstance(name, int):
        if not name in range(0,10):
            raise NameError('name should be between 0 and 9')
        
        return globals.template.template.addNumericLabel(name)
        
    else:
        return globals.template.template.addLabel(name,False)
        
def global_label(name):
    return globals.template.template.addLabel(name,True)
    
def weak(name):
    return globals.template.template.addWeakLabel(name)
    
def label_b(index):
    return globals.template.numeric_label_ref(index, False)
    
def label_f(index):
    return globals.template.numeric_label_ref(index, True)
    
def get_address_of(label):
    return globals.template.template.getAddressForLabel(label)

def exception_handler(attrs = {},contents = lambda : []):
    org = attrs.get('org')
    exception = attrs.get('exception')
    id = attrs.get('id')
    if id is None:
        id = ''
    builder = globals.template.template.beginExceptionHandler(id)
    instance = attrs.get('instance')
    if instance is None:
        instance = RangeClass(0,get_option_value('instance-number')-1)
    
    if isinstance(instance,RangeClass):
        print "min is {}\n max is {}\n".format(instance.min,instance.max)
        builder.setInstances(instance.min,instance.max)
    else:
        builder.setInstances(instance)
        
    def entry_point(org,exception,contents = lambda : []):
        from java.math import BigInteger
        builder.beginEntryPoint(BigInteger(str(org)), exception)
        contents()
        return builder.endEntryPoint()
        
    entry_point(org,exception,contents)
    
    return globals.template.template.endExceptionHandler()




def set_option_value(name,value):
    from ru.ispras.microtesk.test import TestEngine
    engine = TestEngine.getInstance()
    return engine.setOptionValue(name, value)  
    
def get_option_value(name):
    from ru.ispras.microtesk.test import TestEngine
    engine = TestEngine.getInstance()
    return engine.getOptionValue(name)
    
def rev_id():
    from ru.ispras.microtesk.test import TestEngine
    engine = TestEngine.getInstance()
    return engine.getModel.getRevisionId()
    
def is_rev(id):
    from ru.ispras.microtesk.test import TestEngine
    engine = TestEngine.getInstance()
    return engine.isRevision(id)
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
        
        
        
        
        
        
        
        
        
        
        
        
             
     
     
     
     
     
     
     
     
     
     
     
     
     
        