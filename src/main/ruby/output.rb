#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# output.rb, May 19, 2014 11:19:42 AM Andrei Tatarnikov
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
# The Output module provides methods to print textual information into
# the simulator output or to insert it into the generated test program.
# The module is included into test templates (as a mixin for the Template
# class) so that its methods are available in all test templates and can
# be used in their code.
#
# Preconditions:
# 
# 1. It is expected that the Template class that imports the Output module
# as a mixin implements the add_output method that registers output objects
# created by methods provided by the Output module.
#
# 2. It is expected that the class that imports the Output module provides
# the following attributes:
#     (1) sl_comment_starts_with,
#     (2) ml_comment_starts_with,
#     (3) ml_comment_ends_with.
# They are used to form comments. In the current implementation, they are
# defined in the Settings module that is included in the Template class
# as a mixin. 
#
module Output

  #
  # Description:
  #
  # The Output class is wrapper class that holds an instance of the Java Output
  # class (ru.ispras.microtesk.test.template.Output) that holds information to
  # be printed to the simulator output or to the test program. The class will
  # become redundant when the generation logic will be implemented in Java. 
  #  
    class Output
    # Returns the Java object Output.
    attr_reader :java_object 
  
    def initialize(java_object)
      @java_object = java_object
    end
  end

  #
  # Description:
  #
  # The Location class describes an access to a specific location (register or
  # memory address) performed when prining data.
  #
  class Location
    attr_reader :name, :index 
      
    def initialize(name, index)
      @name  = name
      @index = index
    end
  end

  #
  # Creates a location-based format argument for format-like output methods. 
  #
  def location(name, index)
    Location.new name, index
  end

  #
  # Prints text into the simulator execution log.
  #
  def trace(format, *args)
    print_format true, format, *args
  end

  # 
  # Adds the new line character into the test program
  #
  def newline
    text '' 
  end

  # 
  # Adds text into the test program.
  #
  def text(format, *args)
    print_format false, format, *args
  end
  
  # 
  # Adds a comment into the test program (uses sl_comment_starts_with).
  #
  def comment(format, *args)
    text sl_comment_starts_with + string
  end

  #
  # Starts a multi-line comment (uses sl_comment_starts_with)
  #
  def start_comment
    text ml_comment_starts_with
  end

  #
  # Ends a multi-line comment (uses ml_comment_ends_with)
  #
  def end_comment
    text ml_comment_ends_with 
  end

  #
  # Prints a format-based output to the simulator log or to the test program
  # depending of the is_runtime flag.
  #
  def print_format(is_runtime, format, *args)
    java_import Java::Ru.ispras.microtesk.test.template.OutputBuilder
    builder = OutputBuilder.new is_runtime, format

    args.each do |arg|
      if arg.is_a?(Integer) or arg.is_a?(String) or 
         arg.is_a?(TrueClass) or arg.is_a?(FalseClass)
         builder.addArgument arg
      elsif arg.is_a?(Location)
        builder.addArgument arg.name, arg.index 
      else
        raise MTRubyError, "Illegal format argument class #{arg.class}"
      end  
    end

    o = Output.new(builder.build)
    add_output(o)
  end

end # module Output
