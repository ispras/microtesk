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

from types import MethodType

import template

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
 

        
def define_addressing_mode(mode):
    
    name  = mode.getName()
    
    #print "Defining mode {}...".format(name)
    
    
    def p(self,*arguments, **kwargs):
        
        
        builder = self.template.newAddressingModeBuilder(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        if situations != None:
            pass
        else:
            default_situation = self.template.getDefaultSituation(name)
            if default_situation != None:
                builder.setSituation(default_situation)
            
        return builder.build()
    
    define_method_for(template.Template, name, "mode", p)
    
def define_addressing_mode_group(name):
    
    #print "Defining mode group {}...".format(name)
    
    def p(self,*arguments, **kwarg):
        builder = self.template.newAddressingModeBuilderForGroup(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        if situations != None:
            pass
        else:
            default_situation = self.template.getDefaultSituation(group_name)
            if default_situation == None:
                default_situation = self.template.getDefaultSituation(name)
             
            if default_situation != None:
                builder.setSituation(default_situation) 
        
        return builder.build()
    
    define_method_for(template.Template, name, "mode", p)

def define_operation(op):
    name = op.getName()
    
    #print "Defining operation {}...".format(name)
    
    is_root = op.isRoot()
    root_shortcuts = op.hasRootShortcuts()
    
    def p(self,*arguments, **kwargs):
        builder = self.template.newOperationBuilder(name)
        set_argumets(builder, arguments)
        situations = kwargs.get('situations')
        
        
        if situations != None:
            pass
        else:
            default_situation = self.template.getDefaultSituation(name)
            if default_situation != None:
                builder.setSituation(default_situation)
                
        if is_root:
            self.template.setRootOperation(builder.build(),self.get_caller_location())
            self.template.endBuildingCall
        elif root_shortcuts:
            builder.setContext("#root")
            self.template.setRootOperation(builder.build(),self.get_caller_location())
            self.template.endBuildingCall
            
        else:
            return builder
        
    define_method_for(template.Template, name, "op", p)
    
def define_operation_group(group_name):
    
    #print "Defining operation group {}...".format(group_name)
    
    def p(self,*arguments, **kwargs):
        op = self.template.chooseMetaOperationFromGroup(group_name)
        name = op.getName()
        situations = kwargs.get('situations')
    
        is_root = op.isRoot()
        root_shortcuts = op.hasRootShortcuts()
        
        builder = self.template.newOperationBuilder(name)
        set_argumets(builder, arguments)
        
        
        if situations != None:
            pass
        else:
            default_situation = self.template.getDefaultSituation(group_name)
            if default_situation == None:
                default_situation = self.template.getDefaultSituation(name)
            
            if default_situation != None:
                builder.setSituation(default_situation)
                
        if is_root:
            self.template.setRootOperation(builder.build(),self.get_caller_location())
            self.template.endBuildingCall
        elif root_shortcuts:
            builder.setContext("#root")
            self.template.setRootOperation(builder.build(),self.get_caller_location())
            self.template.endBuildingCall
            
        else:
            return builder
        
    define_method_for(template.Template, group_name, "op", p)
        
    
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
    
    #print "Defining method {}.{}".format(target_class,method_name)
    
    if not hasattr(target_class, method_name):
        setattr(target_class,method_name,MethodType(method_body,None,target_class))
    elif not hasattr(target_class, "{}_{}".format(method_type,method_name)):
        setattr(target_class,"{}_{}".format(method_type,method_name),MethodType(method_body,None,target_class))
    else:
        print "Error: Failed to define the {} method ({})".format(method_type,method_name)
    
    
    
    
    
    
    
    
    
    
    
    
    