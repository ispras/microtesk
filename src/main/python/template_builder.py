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

import template

def define_runtime_methods(metamodel):
    
    modes = metamodel.getAddressingModes()
    for x in modes:
        define_addressing_mode(mode)
    
    mode_groups = metamodel.getAddressingModeGroups()
    for x in mode_groups:
        define_addressing_mode_group(mode_group.getName())
        
    ops = metamodel.getOperations()
    for x in ops:
        define_operation(op)
        
    op_groups = metamodel.getOperationGroups()
    for x in op_groups:
        define_operation_group(op_group.getName())
 

        
def define_addresing_mode(mode):
    
    name  = mode.getName()
    
    def p(*arguments, **kwargs):
    
        builder = self.template.newAddressingModeBuilder(name)
        set_argumets(builder, arguments)
        
        if situations != None:
            builder.setSituation(self.situation_manager.eval(situations))
        else:
            default_situation = self.template.getDefaultSituation(name)
            if default_situation != None:
                builder.setSituation(default_situation)
            
        builder.build()
    
    define_method_for(Template, name, "mode", p)
    
    
def set_argumets(builder,args): 
    if len(args) == 1 and type(args[0]) is dict:
        set_arguments_from_hash(builder, args.first())
    else:
        set_arguments_from_array(builder, args)
        
        
def set_arguments_from_hash(builder,args):
    for name, value in args.iteritems():
        if type(value) is WrappedObject:
            value = value.java_object 
        if type(value) is String:
            value = value;
        builder.setArgument(name,value)
        
        
def set_arguments_from_array(builder,args):
    for value in args:
        if type(args) is list:
            set_arguments_from_array(builder, value)
        else:
            if type(value) is WrappedObject:
                value = value.java_object 
            if type(value) is String:
                value = value;
            builder.addArgument(value)
    
        
def define_method_for(target_class, method_name, method_type, method_body):
    
    if not hasattr(target_class, method_name):
        setattr(target_class,method_name,method_body)
    elif not hasattr(target_class, "{}_{}".format(method_type,method_name)):
        setattr(target_class,"{}_{}".format(method_type,method_name),method_body)
    else:
        print "Error: Failed to define the {} method ({})".format(method_type,method_name)
    
    
    
    
    
    
    
    
    
    
    
    
    