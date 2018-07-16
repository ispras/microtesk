#
# Copyright 2018 ISP RAS (http://www.ispras.ru)
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
# The 'Operations' module provides methods to describe operations with dynamically
# generated immediate operands (e.g. random values, label addresses, unknown values etc.).
#
module Operators

  def _AND(operand1, operand2)
    new_binary_operation('AND', operand1, operand2)
  end

  def _OR(operand1, operand2)
    new_binary_operation('OR', operand1, operand2)
  end

  def _XOR(operand1, operand2)
    new_binary_operation('XOR', operand1, operand2)
  end

  def _ADD(operand1, operand2)
    new_binary_operation('ADD', operand1, operand2)
  end

  def _SUB(operand1, operand2)
    new_binary_operation('SUB', operand1, operand2)
  end

  def _PLUS(operand)
    new_unary_operation('PLUS', operand)
  end

  def _MINUS(operand)
    new_unary_operation('MINUS', operand)
  end

  def _NOT(operand)
    new_unary_operation('NOT', operand)
  end

  private

  def new_binary_operation(operator, operand1, operand2)
    java_import Java::Ru.ispras.microtesk.test.template.OperatorValueFactory
    OperatorValueFactory.newBinaryOperator operator, operand1, operand2
  end

  def new_unary_operation(operator, operand)
    java_import Java::Ru.ispras.microtesk.test.template.newUnaryOperator
    OperatorValueFactory.newUnaryOperator operator, operand
  end

end
