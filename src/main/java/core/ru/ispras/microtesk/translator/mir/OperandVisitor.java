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

import java.util.List;

import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Ite;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.Instruction.*;

abstract class OperandVisitor<T> {
  public T visitConst(Constant opnd) { return visitOperand(opnd); }
  public T visitLhs(Local opnd) { return visitOperand(opnd); }
  public T visitLvalue(Lvalue opnd) { return visitOperand(opnd); }
  public T visitLocal(Local opnd) { return visitOperand(opnd); }
  public T visitField(Field opnd, T base) { return visitOperand(opnd); }
  public T visitIndex(Index opnd, T base, T index) { return visitOperand(opnd); }
  public T visitStatic(Static opnd) { return visitOperand(opnd); }
  public T visitClosure(Closure opnd, List<T> upvalues) { return visitOperand(opnd); }
  public T visitIte(Ite opnd, T guard, T taken, T other) { return visitOperand(opnd); }

  public abstract T visitOperand(Operand opnd);
}
