/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

enum UnOpcode implements BinOpcode {
  Use;

  public Rvalue make(final Operand opnd) {
    return new Rvalue(this, opnd, null);
  }

  public Rvalue make(final Operand lhs, final Operand rhs) {
    if (lhs != null) {
      return new Rvalue(this, lhs, rhs);
    }
    return new Rvalue(this, rhs, lhs);
  }

  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    if (lhs != null) {
      return lhs.getType();
    }
    return rhs.getType();
  }
}
