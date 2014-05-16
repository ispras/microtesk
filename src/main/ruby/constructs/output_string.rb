#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# output_string.rb, May 16, 2014 4:03:05 PM
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
# The OutputString class holds text that will be inserted into the generated
# test program or into the MicroTESK simulator output.
#

class OutputString

  attr_reader :text

  #
  # Initializes a new OutputString object.
  #
  # Arguments:
  #   text Text to be printed.
  #   is_runtime Specifies whether the text will be printed to the MicroTESK 
  #              simulator output during simulation or inserted into the 
  #              generated test program after simulation.
  #
  def initialize(text, is_runtime = false)
    @text = text
    @is_runtime = is_runtime
  end

  #
  # Returns true if the text should be evaluated and printed to the MicroTESK
  # simulator output during simulation or false if it should be evaluated and 
  # inserted into the generated test program after simulation.
  #
  def runtime?
    @is_runtime
  end

  #
  # Returns true if the text should be evaluated before printing or false
  # otherwise. Strings to be evaluated are enclosed with double quotes.
  #
  def evaluate?
    !@text.empty? and ?\" == @text[0] and ?\" == @text[-1]
  end

end
