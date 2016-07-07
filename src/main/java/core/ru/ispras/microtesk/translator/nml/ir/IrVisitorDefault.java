/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.nml.ir;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public class IrVisitorDefault implements IrVisitor {
  private Status status;

  public IrVisitorDefault() {
    this.status = Status.OK;
  }

  @Override
  public final Status getStatus() {
    return status;
  }

  @Override
  public final void setStatus(final Status status) {
    InvariantChecks.checkNotNull(status);
    this.status = status;
  }

  public final boolean isStatus(final Status status) {
    return getStatus() == status;
  }

  @Override
  public void onBegin() {}

  @Override
  public void onEnd() {}

  @Override
  public void onResourcesBegin() {}

  @Override
  public void onResourcesEnd() {}

  @Override
  public void onLetConstant(LetConstant let) {}

  @Override
  public void onLetLabel(LetLabel let) {}

  @Override
  public void onType(String name, Type type) {}

  @Override
  public void onMemory(String name, MemoryExpr memory) {}

  @Override
  public void onPrimitivesBegin() {}

  @Override
  public void onPrimitivesEnd() {}

  @Override
  public void onPrimitiveBegin(Primitive item) {}

  @Override
  public void onPrimitiveEnd(Primitive item) {}

  @Override
  public void onAlternativeBegin(PrimitiveOR orRule, Primitive item) {}

  @Override
  public void onAlternativeEnd(PrimitiveOR orRule, Primitive item) {}

  @Override
  public void onArgumentBegin(PrimitiveAND andRule, String argName, Primitive argType) {}

  @Override
  public void onArgumentEnd(PrimitiveAND andRule, String argName, Primitive argType) {}

  @Override
  public void onAttributeBegin(PrimitiveAND andRule, Attribute attr) {}

  @Override
  public void onAttributeEnd(PrimitiveAND andRule, Attribute attr) {}

  @Override
  public void onStatement(PrimitiveAND andRule, Attribute attr, Statement stmt) {}

  @Override
  public void onShortcutBegin(PrimitiveAND andRule, Shortcut shortcut) {}

  @Override
  public void onShortcutEnd(PrimitiveAND andRule, Shortcut shortcut) {}

  @Override
  public void onAssignment(StatementAssignment stmt) {}

  @Override
  public void onAttributeCallBegin(StatementAttributeCall stmt) {}

  @Override
  public void onAttributeCallEnd(StatementAttributeCall stmt) {}

  @Override
  public void onConditionBegin(StatementCondition stmt) {}

  @Override
  public void onConditionEnd(StatementCondition stmt) {}

  @Override
  public void onConditionBlockBegin(Node condition) {}

  @Override
  public void onConditionBlockEnd(Node condition) {}

  @Override
  public void onFormat(StatementFormat stmt) {}

  @Override
  public void onFunctionCall(StatementFunctionCall stmt) {}
}
