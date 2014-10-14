#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# demo_prepost.rb, May 19, 2014 5:57:01 PM
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

require ENV['TEMPLATE']

#
# Description:
#
# The purpose of the DemoPrepost is to demonstrate how code of test templates
# can be reused by other test templates. The class provides implementations
# of the pre and post methods that are the initialization and finalization
# sections respectively. These method can be reused by other test templates
# using the mechanism of class inheritance.
#
class DemoPrepost < Template

  def initialize
    super

    # This means that DemoPrepost is designed for reuse only. It is an abstract
    # class to be inherited by other test templates and it cannot be used as an
    # independent test template to generate a test program.

    @is_executable = false
  end

  def pre
    trace 'Initialization:'
    comment 'Initialization Section Starts'
    add mem(:i => 12), mem(:i => 13)
    comment 'Initialization Section Ends'
    newline
  end

  def post
    newline
    trace 'Finalization:'
    comment 'Finalization Section Starts'
    add mem(:i => 23), imm(23)
    comment 'Finalization Section Ends'
  end

end
