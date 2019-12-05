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
from minimips_base import MiniMipsBaseTemplate
from minimips_base import *
from template import *

class BlockTemplate(MiniMipsBaseTemplate):
    def __init__(self):
        MiniMipsBaseTemplate.__init__(self)
        
    def run(self):
        sequence({},
                lambda : [
                    add(t0(),t1(),t2()),
                    sub(t3(),t4(),t5()),
                    AND(reg(u_()),reg(u_()),reg(u_()))
                    ]
                ).run()
        atomic({},
                lambda : [
                    add(t0(),t1(),t2()),
                    sub(t3(),t4(),t5()),
                    AND(reg(u_()),reg(u_()),reg(u_()))
                    ]
                ).run()
        iterate({},
                lambda : [
                    epilogue(lambda : [nop()]),
                    add(t0(),t1(),t2()),
                    sub(t3(),t4(),t5()),
                    AND(reg(u_()),reg(u_()),reg(u_()))
                    ]
                ).run()
        block({'combinator' : 'product', 'compositor' : 'random'},
                lambda : [
                    epilogue(lambda : [nop()]),
                    iterate({},
                            lambda : [
                                add(t0(),t1(),t2()),
                                sub(t3(),t4(),t5())
                                ]
                            ),
                    iterate({},
                            lambda : [
                                AND(reg(u_()),reg(u_()),reg(u_())),
                                nop()
                                ]
                            )
                    ]
                ).run()
        block({'combinator' : 'diagonal', 'compositor' : 'random','obfuscator' : 'random'},
                lambda : [
                    sequence({},
                            lambda : [
                                add(t0(),t1(),t2()),
                                sub(t3(),t4(),t5()),
                                OR(t7(),t8(),t9())
                                ]
                            ),
                    atomic({},
                            lambda : [
                                prologue(lambda : [comment('Atomic starts')]),
                                epilogue(lambda : [comment('Atomic ends')]),
                                
                                AND(reg(u_()),reg(u_()),reg(u_())),
                                nop()
                                ]
                            )
                    ]
                ).run()
                
globals.template = BlockTemplate()
globals.template.generate()
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
