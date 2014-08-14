#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# engine.rb, May 7, 2014 4:41:22 PM Andrei Tatarnikov
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

#
# Description:
#
# The Engine module is a wrapper round the TestEngine class provided
# by MicroTESK (ru.ispras.microtesk.test.TestEngine). It provides methods
# to build objects describing instruction call blocks and instuction call 
# sequences (abstract and concrete). TODO: In the future, it is recommended
# to consider renaming the module into MicroTESK (it will contain all 
# wrappers for the Java part).
#

module Engine

  def self.model=(j_model)
    java_import Java::Ru.ispras.microtesk.test.TestEngine
    @@j_test_engine = TestEngine.getInstance(j_model)

    @@j_bbf = @@j_test_engine.getBlockBuilders()
    @@j_dg  = @@j_test_engine.getDataGenerator()
  end

  def self.j_bbf
    @@j_bbf
  end

  def self.generate_data(abstract_sequence)
    @@j_dg.generate(abstract_sequence)
  end

  def self.process(sequence_it)
    @@j_test_engine.process sequence_it
  end

end
