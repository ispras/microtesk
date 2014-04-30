#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# argument.rb, Apr 30, 2014 7:13:07 PM Andrei Tatarnikov
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
# The Argument class stores information about an instruction call argument.
# This information includes the name of the addressing mode being used 
# and arguments passed to the addressing mode.
#  
class Argument

  # Addressing mode name
  #
  attr_accessor :mode

  # Table of arguments of the addressing mode, where
  # key (String) is argument name and value (Object) is argument value
  #
  attr_accessor :values

  #
  # Initializes a new instance of an Argument object.
  #
  def initialize
    @mode   = "UntitledMode"
    @values = Hash.new
  end

  #
  # Creates an instance of a Java Argument object that is used in 
  # AbstractCall object.
  #
  # Parameters:
  #   j_arg_builder A Java ArgumentBuilder object
  #   (ru.ispras.microtesk.test.block.ArgumentBuilder) that creates
  #   a Java Argument object  
  #
  # Returns:
  #   A new Java Argument object (ru.ispras.microtesk.test.block.Argument)
  #
  def build(j_arg_builder)

    @values.each_pair do |key, value|
      if value.is_a? NoValue
        #  Handle NoValue
        j_arg_builder.setRandomArgument(key)
      else
        j_arg_builder.setArgument(key, value)
      end
    end

    j_arg_builder.build()
  end

end
