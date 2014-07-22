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
# It is expected that the Template class that imports the Output module
# as a mixin implements the add_output method that registers output objects
# created by methods provided by the Output module.
#
module Output

#
# Description:
#
# The Output class is an abstract base class for objects that contain
# information to be printed to the simulation output to inserted into
# the generated test program. 
#
class Output

  #
  # Initializes a new OutputCode object.
  #
  # Arguments:
  #   is_runtime Specifies whether evalution will be performed during simulation
  #              or after. In the former case, the results will be printed to
  #              the MicroTESK simulator output. In the latter case, the results  
  #              will be inserted into the generated test program.
  #
  def initialize(is_runtime)
     @is_runtime = is_runtime
  end

  #
  # Returns true if it should be evaluated during simulation and the
  # evaluation results should be printed to the MicroTESK simulator
  # output or false if it should be evaluated after simulation and
  # the results should be inserted into the generated test program.
  #
  def runtime?
    @is_runtime
  end

  #
  # An abstract method to be implemented by descendants. Evaluates the stored
  # information in the context of the provided object and returns its textual
  # representation.
  #
  def evaluate_to_text(object)
    raise "Not implemented!"
  end

  #
  # Returns information on the current object in a textual form.
  #
  def to_s
    "Class: #{self.class}, Runtime: #{runtime?}"
  end

end

#
# Description:
#
# The OutputCode class holds code (a Proc object) that will be executed during
# test program generation. The result of its execution will be inserted
# into the generated test program or into the MicroTESK simulator output.
#
class OutputCode < Output

  #
  # Initializes a new OutputCode object.
  #
  # Arguments:
  #   text Code to be executed (a Proc object) to produce a text.
  #   is_runtime Specifies whether the code will be executed during simulation
  #              or after. In the former case, the results will be printed to
  #              the MicroTESK simulator output. In the latter case, the results  
  #              will be inserted into the generated test program.
  #
  def initialize(proc, is_runtime = false)
    super(is_runtime)
     
    if nil == proc or !proc.is_a? Proc
      raise "Unsupported argument type!" 
    end
 
    @proc = proc
  end

  #
  # Executes the stored procedure in the context of the provided object.
  # All members of the provided object are accesible from the stored
  # procedure.
  #
  # Arguments:
  #   object An object provides an execution context for the stored procedure.   
  #
  def evaluate_to_text(object)
    object.instance_exec &@proc
  end

  #
  # Returns information on the current object in a textual form.
  #
  def to_s
    "#{super.to_s}, Contents: #{@proc}"
  end

end

#
# Description:
#
# The OutputString class holds text that will be inserted into the generated
# test program or into the MicroTESK simulator output.
#
class OutputString < Output

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
    super(is_runtime)
    
    if nil == text or !text.is_a? String
      raise "Unsupported argument type!" 
    end
    
    @text = text
  end

  #
  # Evaluates the stored text in the context of the provided object (if required).
  # All members of the provided object are accesible during the evaluation.
  #
  # Arguments:
  #   object An object provides an evaluation context.   
  #
  def evaluate_to_text(object)
    if evaluate? then
      object.instance_eval @text
    else
      @text
    end
  end

  #
  # Returns true if the text should be evaluated before printing or false
  # otherwise. Strings to be evaluated are enclosed with double quotes.
  #
  def evaluate?
    !@text.empty? and ?\" == @text[0] and ?\" == @text[-1] #"
  end

  #
  # Returns information on the current object in a textual form.
  #
  def to_s
    "#{super.to_s}, Contents: #{@text}"
  end

end

#
# Prints text into the simulator execution log.
#
def trace(string)
  add_output OutputString.new(string, true)
end

#
# Evaluates a code block at simulation time and prints the resulting
# text into the simulator output.
#
def trace_(&block)
  add_output OutputCode.new(block, true)
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
def text(string)
  add_output OutputString.new(string)
end
 
#
# Evaluates a code block at printing time and puts the resulting
# text into the test program.
#  
def text_(&block)
  add_output OutputCode.new(block)
end

# 
# Adds a comment into the test program (uses @sl_comment_starts_with).
#
def comment(string)
  if !string.empty? and string[0] == ?\" then #"
    text string.insert(1, @sl_comment_starts_with)
  else
    text @sl_comment_starts_with + string
  end
end

#
# Starts a multi-line comment (uses @sl_comment_starts_with)
#
def start_comment
  text @ml_comment_starts_with
end

#
# Ends a multi-line comment (uses the ML_COMMENT_ENDS_WITH property)
#
def end_comment
  text @ml_comment_ends_with 
end

end # module Output
