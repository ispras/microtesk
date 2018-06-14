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
import globals
import sys
from ru.ispras.microtesk import SysUtils
#import __main__

HOME = SysUtils.getHomeDir()
PYTHON = HOME + "/lib/python/"
TEMPLATE = HOME + "/arch/demo/minimips/templates"
globals.TEMPLATE_FILE = sys.argv[0]
sys.path.append(TEMPLATE) 
sys.path.append(PYTHON)
#sys.path.append("/home/luesal/jython2.7.0/Lib")

def prepare_template_classes():
    try:
        template_file = __import__("empty")
    except ImportError:
        print('An error occured trying to import the file.')
    return Template.template_classes

#def main():
    #print("test")
    #template_file.test()
    #template.test()
   # template_classes = prepare_template_classes()
    #for i in template_classes:
     #       print i,template_classes[i]
    #template = Template()
    #template.generate()
        
    
    
#if __name__ == "__main__":
#    main()
#execfile('template_builder.py')

from ru.ispras.microtesk.test import TestEngine
import template_builder
engine = TestEngine.getInstance()
template_builder.define_runtime_methods(engine.getModel().getMetaData())

execfile(globals.TEMPLATE_FILE)
