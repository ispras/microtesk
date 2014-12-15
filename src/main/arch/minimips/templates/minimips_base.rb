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
    data_config(:text => '.data', :target => 'M', :addressableSize => 8) {
      define_type :id => :byte, :text => '.byte', :type => type('card', 8)
      define_type :id => :half, :text => '.half', :type => type('card', 16)
      define_type :id => :word, :text => '.word', :type => type('card', 32)

      define_space :id => :space, :text => '.space', :fillWith => 0
      define_ascii_string :id => :ascii,  :text => '.ascii',  :zeroTerm => false
      define_ascii_string :id => :asciiz, :text => '.asciiz', :zeroTerm => true
    }

    pseudo_instruction(:id => 'la', :format => "la %s, %d", :args => ['rd', 'addr']) {
      lui rd, addr(16, 31)
      ori rd, rd, addr(0, 15)
    }
  end

  def post
    # Place your finalization code here
  end

  def gpr(index)
    location('GPR', index)
  end
end