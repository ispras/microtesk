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

#from ru.ispras.microtesk.test import TestEngine
#engine = TestEngine.getInstance()
#define_runtime_methods(engine.getModel().getMetaData())


import template
import globals
#import importlib
#import ntpath

#dummy = ntpath.basename(globals.TEMPLATE_FILE[:-3])
#template_e = importlib.import_module(dummy)

#print template_e.template


from java.math import BigInteger



def define_runtime_methods(metamodel):
    
    modes = metamodel.getAddressingModes()
    for x in modes:
        define_addressing_mode(x)
    
    mode_groups = metamodel.getAddressingModeGroups()
    for x in mode_groups:
        define_addressing_mode_group(x.getName())
        
    ops = metamodel.getOperations()
    for x in ops:
        define_operation(x)
        
    op_groups = metamodel.getOperationGroups()
    for x in op_groups:
        define_operation_group(x.getName())
        
    registers = metamodel.getRegisters()
    for x in registers:
        define_store(x)
    
    memories = metamodel.getMemoryStores()
    for x in memories:
        define_store(x)
 

def define_store(store):
  name = store.getName()

  def p(index):
    return template.location(name, index)

  define_method_for(template, name, "store", p)
        
def define_addressing_mode(mode):
    
    name  = mode.getName()
    
    #print "Defining mode {}...".format(name)
    
    
    def p(*arguments, **kwargs):
        
        
        builder = globals.template.template.newAddressingModeBuilder(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        if situations != None:
            pass
        else:
            default_situation = globals.template.template.getDefaultSituation(name)
            if default_situation != None:
                builder.setSituation(default_situation)
            
        return builder.build()
    
    define_method_for(template, name, "mode", p)
    
def define_addressing_mode_group(name):
    
    #print "Defining mode group {}...".format(name)
    
    def p(*arguments, **kwarg):
        builder = globals.template.template.newAddressingModeBuilderForGroup(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        if situations != None:
            pass
        else:
            default_situation = globals.template.template.getDefaultSituation(group_name)
            if default_situation == None:
                default_situation = globals.template.template.getDefaultSituation(name)
             
            if default_situation != None:
                builder.setSituation(default_situation) 
        
        return builder.build()
    
    define_method_for(template, name, "mode", p)

def define_operation(op):
    name = op.getName()
    
    #print "Defining operation {}...".format(name)
    
    is_root = op.isRoot()
    root_shortcuts = op.hasRootShortcuts()
    def p(*arguments, **kwargs):
        builder = globals.template.template.newOperationBuilder(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        
        if situations != None:
            pass
        else:
            default_situation = globals.template.template.getDefaultSituation(name)
            if default_situation != None:
                builder.setSituation(default_situation)
                
        if is_root:
            globals.template.template.setRootOperation(builder.build(),globals.template.get_caller_location())
            globals.template.template.endBuildingCall()
        elif root_shortcuts:
            builder.setContext("#root")
            globals.template.template.setRootOperation(builder.build(),globals.template.get_caller_location())
            globals.template.template.endBuildingCall()
            
        else:
            return builder
        
    define_method_for(template, name, "op", p)
    
def define_operation_group(group_name):
    
    #print "Defining operation group {}...".format(group_name)
    
    def p(*arguments, **kwargs):
        op = globals.template.template.chooseMetaOperationFromGroup(group_name)
        name = op.getName()
        situations = kwargs.get('situations')
    
        is_root = op.isRoot()
        root_shortcuts = op.hasRootShortcuts()
        
        builder = globals.template.template.newOperationBuilder(name)
        set_argumets(builder, arguments)
        
        
        if situations != None:
            pass
        else:
            default_situation = globals.template.template.getDefaultSituation(group_name)
            if default_situation == None:
                default_situation = globals.template.template.getDefaultSituation(name)
            
            if default_situation != None:
                builder.setSituation(default_situation)
                
        if is_root:
            globals.template.template.setRootOperation(builder.build(),globals.template.get_caller_location())
            globals.template.template.endBuildingCall()
        elif root_shortcuts:
            builder.setContext("#root")
            globals.template.template.setRootOperation(builder.build(),globals.template.get_caller_location())
            globals.template.template.endBuildingCall()
            
        else:
            return builder
        
    define_method_for(template, group_name, "op", p)
        
    
def set_argumets(builder,args): 
    if len(args) == 1 and type(args[0]) is dict:
        set_arguments_from_hash(builder, args.first())
    else:
        set_arguments_from_array(builder, args)
        
        
def set_arguments_from_hash(builder,args):
    for name, value in args.iteritems():
        if isinstance(value, template.WrappedObject):
            value = value.java_object 
        if isinstance(value, basestring):
            value = value
        if isintance(value,int):
            builder.setArgument(name,BigInteger(str(value)))
        else:
            builder.setArgument(name,value)
        
        
def set_arguments_from_array(builder,args):
    for value in args:
        if type(args) is list:
            set_arguments_from_array(builder, value)
        else:
            if isinstance(value, template.WrappedObject):
                value = value.java_object 
            if isinstance(value, basestring):
                value = value
            if isinstance(value,int):
                builder.addArgument(BigInteger(str(value)))
            else:
                builder.addArgument(value)
    
        
def define_method_for(target_class, method_name, method_type, method_body):
    method_name = method_name.lower()
    
    if method_name == "or" or method_name == "and":
        method_name = method_name.upper()
    
    print "Defining method {}.{}".format(target_class,method_name)
    
    if not hasattr(target_class, method_name):
        #setattr(target_class,method_name,MethodType(method_body,None,target_class))
        setattr(target_class, method_name, method_body)
    elif not hasattr(target_class, "{}_{}".format(method_type,method_name)):
     #   setattr(target_class,"{}_{}".format(method_type,method_name),MethodType(method_body,None,target_class))
        setattr(target_class,"{}_{}".format(method_type,method_name),method_body)
    else:
        print "Error: Failed to define the {} method ({})".format(method_type,method_name)
    
    
    
    
    
    
    
    
    
    
    
    
    