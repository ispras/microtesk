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

class DemoPrepost < Template
  def initialize
    super
    @is_executable = false
  end

  def pre
    comment "Precondition Section"
    add mem("i" => 12), mem("i" => 13)
  end

  def post
    comment "Post Condition Section"
    add mem("i" => 23), imm(23)
  end
end
