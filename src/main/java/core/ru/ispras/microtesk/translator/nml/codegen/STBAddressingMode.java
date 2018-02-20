/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;

import java.math.BigInteger;
import java.util.Map;

final class STBAddressingMode extends STBPrimitiveBase {
  private final String modelName;
  private final PrimitiveAND mode;

  public STBAddressingMode(
      final String modelName,
      final PrimitiveAND mode) {

    assert mode.getKind() == Primitive.Kind.MODE;

    this.modelName = modelName;
    this.mode = mode;
  }

  private void buildHeader(final STGroup group, final ST t) {
    t.add("name", mode.getName());
    t.add("type", null != mode.getReturnType() ? mode.getReturnType().getJavaText() : "null");
    t.add("pack", String.format(PackageInfo.MODE_PACKAGE_FORMAT, modelName));

    t.add("imps", Map.class.getName());
    t.add("imps", BigInteger.class.getName());
    t.add("imps", ru.ispras.microtesk.model.Execution.class.getName());
    t.add("imps", ru.ispras.microtesk.model.ProcessingElement.class.getName());
    t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", IsaPrimitive.class.getPackage().getName()));
    t.add("imps", ru.ispras.microtesk.model.memory.Location.class.getName());
    t.add("imps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".PE", modelName));
    t.add("imps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TempVars", modelName));
    t.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));
    t.add("base",  IsaPrimitive.class.getSimpleName());
  }

  private void buildArguments(final STGroup group, final ST t) {
    for (final Map.Entry<String, Primitive> e : mode.getArguments().entrySet()) {
      final String argName = e.getKey();
      final Primitive argType = e.getValue();

      t.add("param_names", argName);
      t.add("param_types", argType.getName());
    }
  }

  private void buildAttributes(final STGroup group, final ST t) {
    for (final Attribute attr : mode.getAttributes().values()) {
      final ST attrST = group.getInstanceOf("mode_attribute");

      attrST.add("name", attr.getName());
      attrST.add("rettype", getRetTypeName(attr.getKind()));
      attrST.add("usePE",
          Attribute.Kind.ACTION == attr.getKind()
              && !attr.getName().equals(Attribute.INIT_NAME));

      if (Attribute.Kind.ACTION == attr.getKind()) {
        for (final Statement stmt : attr.getStatements()) {
          addStatement(attrST, stmt, false);
        }
      } else if (Attribute.Kind.EXPRESSION == attr.getKind()) {
        final int count = attr.getStatements().size();
        int index = 0;
        for (final Statement stmt : attr.getStatements()) {
          addStatement(attrST, stmt, index == count - 1);
          index++;
        }
      } else {
        assert false : "Unknown attribute kind: " + attr.getKind();
      }

      attrST.add("override", attr.isStandard());
      t.add("attrs", attrST);
    }
  }

  private void buildReturnExpession(final ST t) {
    final Expr returnExpr = mode.getReturnExpr();

    if (null == returnExpr) {
      t.add("ret", false);
      return;
    }

    final NodeInfo returnExprNodeInfo = returnExpr.getNodeInfo();
    if (returnExprNodeInfo.isLocation() && !returnExprNodeInfo.isCoersionApplied()) {
      t.add("ret", PrinterLocation.toString((Location) returnExprNodeInfo.getSource()));
    } else {
      t.add("ret", String.format("new Location(%s)", ExprPrinter.toString(returnExpr)));
    }
  }

  @Override
  public ST build(final STGroup group) {
    final ST t = group.getInstanceOf("mode");

    buildHeader(group, t);
    buildArguments(group, t);
    buildAttributes(group, t);
    buildReturnExpession(t);

    return t;
  }
}
