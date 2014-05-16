#
# Copyright (c) 2014 ISPRAS (www.ispras.ru)
#
# Institute for System Programming of Russian Academy of Sciences
#
# 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
#
# All rights reserved.
#
# output_code.rb, May 16, 2014 4:02:41 PM
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
# The OutputCode class holds code (a Proc object) that will be executed during
# test program generation. The result of its execution will be inserted
# into the generated test program or into the MicroTESK simulator output.
#
class OutputCode

  attr_reader :proc

  #
  # Initializes a new OutputCode object.
  #
  # Arguments:
  #   text Code to be execution (a Proc object).
  #   is_runtime Specifies whether the code will be executed during simulation
  #              or after. In the former case, its results will be printed to
  #              the MicroTESK simulator output. In the latter case, its results  
  #              will be inserted into the generated test program.
  #
  def initialize(proc, is_runtime = false)
    @proc = proc
    @is_runtime = is_runtime
  end

  #
  # Returns true if the code should be executed during simulation and its
  # results should be printed to the MicroTESK simulator output or false
  # if it should be executed after simulation and its results should be
  # inserted into the generated test program.
  #
  def runtime?
    @is_runtime
  end

end
