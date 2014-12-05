#
# Copyright 2014 ISP RAS (http://www.ispras.ru)
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

class MinimipsBaseTemplate < Template

  def initialize
    super
    # Initialize settings here 
  end

  def pre
    # Type definitions
    types {
      define_type :id => :byte,     :text => '.byte', :type => type('card', 8)
      define_type :id => :halfword, :text => '.half', :type => type('card', 16)
      define_type :id => :word,     :text => '.word', :type => type('card', 32)

      define_space  :id => :space,  :text => '.space',  :type => type('card', 8), :fillWith => 0
      define_string :id => :ascii,  :text => '.ascii',  :type => type('int', 8),  :zeroTerm => false
      define_string :id => :asciiz, :text => '.asciiz', :type => type('int', 8),  :zeroTerm => true
    }
  end

  def post
    # Place your finalization code here
  end

  def gpr(index)
    location('GPR', index)
  end
end