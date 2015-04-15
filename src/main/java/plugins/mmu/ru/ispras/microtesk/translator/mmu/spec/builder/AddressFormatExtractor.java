/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;
import ru.ispras.microtesk.utils.FortressUtils;

final class AddressFormatExtractor {
  private final IntegerVariableTracker variables;

  private final IntegerVariable address;
  private final IntegerFieldTracker addressFieldTracker;

  private final MmuExpression indexExpr;
  private final MmuExpression tagExpr;
  private final MmuExpression offsetExpr;

  private class Visitor extends ExprTreeVisitorDefault {
    private final List<IntegerField> fields = new ArrayList<>();

    public List<IntegerField> getFields() {
      return fields;
    }

    @Override
    public void onOperationBegin(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVEXTRACT) {
        return;
      }

      if (node.getOperandCount() != 3) {
        throw new IllegalStateException("Wrong operand count (3 is expected): " + node);
      }

      final Node variable = node.getOperand(2);
      if (variable.getKind() != Node.Kind.VARIABLE) {
        return;
      }

      final IntegerVariable integerVariable = 
          variables.getVariable(((NodeVariable)variable).getName());

      if (!address.equals(integerVariable)) {
        return;
      }

      final int lo = FortressUtils.extractInt(node.getOperand(1));
      final int hi = FortressUtils.extractInt(node.getOperand(0));

      fields.add(new IntegerField(address, lo, hi));
      addressFieldTracker.exclude(lo, hi);

      setStatus(Status.SKIP);
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final IntegerVariable integerVariable = variables.getVariable(variable.getName());
      if (!address.equals(integerVariable)) {
        return;
      }

      addressFieldTracker.excludeAll();
      fields.add(new IntegerField(address));
    }
  }

  public AddressFormatExtractor(
      final IntegerVariableTracker variables,
      final IntegerVariable address,
      final Node index,
      final Node match) {
    this.variables = variables;

    this.address = address;
    this.addressFieldTracker = new IntegerFieldTracker(address);

    // TODO: check the format of the "index" expression. Throw exception if needed.
    this.indexExpr = MmuExpression.RCAT(extractFields(index));
    // TODO: check the format of the "match" expression. Throw exception if needed.
    this.tagExpr = MmuExpression.RCAT(extractFields(match));

    this.offsetExpr = MmuExpression.RCAT(addressFieldTracker.getFields());
  }

  private List<IntegerField> extractFields(final Node expr) {
    final Visitor visitor = new Visitor();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    walker.visit(expr);
    return visitor.getFields();
  }

  public MmuExpression getIndexExpr() {
    return indexExpr;
  }

  public MmuExpression getTagExpr() {
    return tagExpr;
  }

  public MmuExpression getOffsetExpr() {
    return offsetExpr;
  }
}
