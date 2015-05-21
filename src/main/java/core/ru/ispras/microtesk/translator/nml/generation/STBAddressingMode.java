/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Map;

import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.expression.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.location.Location;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

final class STBAddressingMode extends STBPrimitiveBase {
  private final String specFileName;
  private final String modelName;
  private final PrimitiveAND mode;

  public STBAddressingMode(String specFileName, String modelName, PrimitiveAND mode) {
    assert mode.getKind() == Primitive.Kind.MODE;

    this.specFileName = specFileName;
    this.modelName = modelName;
    this.mode = mode;
  }

  private void buildHeader(STGroup group, ST t) {
    t.add("name", mode.getName());
    t.add("file", specFileName);
    t.add("pack", String.format(MODE_PACKAGE_FORMAT, modelName));

    t.add("imps", Map.class.getName());
    t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
    t.add("imps", ru.ispras.microtesk.model.api.memory.Location.class.getName());

    t.add("imps", IAddressingMode.class.getName());
    t.add("imps", AddressingMode.class.getName());

    t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));
    t.add("base", AddressingMode.class.getSimpleName());
  }

  private void buildArguments(STGroup group, ST t) {
    for (Map.Entry<String, Primitive> e : mode.getArguments().entrySet()) {
      final String argName = e.getKey();
      final Primitive argType = e.getValue();

      t.add("param_names", argName);
      t.add("param_types", argType.getName());
    }
  }

  private void buildAttributes(STGroup group, ST t) {
    final boolean isInitNeeded = mode.getAttributes().containsKey("init");
    for (Attribute attr : mode.getAttributes().values()) {
      final ST attrST = group.getInstanceOf("mode_attribute");

      attrST.add("name", attr.getName());
      attrST.add("rettype", getRetTypeName(attr.getKind()));

      if (isInitNeeded && !attr.getName().equals("init")) {
        addStatement(attrST, new StatementFunctionCall("init"), false);
      }

      if (Attribute.Kind.ACTION == attr.getKind()) {
        for (Statement stmt : attr.getStatements())
          addStatement(attrST, stmt, false);
      } else if (Attribute.Kind.EXPRESSION == attr.getKind()) {
        assert 1 == attr.getStatements().size() : "Expression attributes must always include a single statement.";

        final Statement stmt =
            (attr.getStatements().size() > 0) ? attr.getStatements().get(0) : null;

        addStatement(attrST, stmt, true);
      } else {
        assert false : "Unknown attribute kind: " + attr.getKind();
      }

      attrST.add("override", isStandardAttribute(attr.getName()));
      t.add("attrs", attrST);
    }
  }

  private void buildReturnExpession(ST t) {
    final Expr returnExpr = mode.getReturnExpr();

    if (null == returnExpr) {
      t.add("ret", false);
      return;
    }

    final NodeInfo returnExprNodeInfo = returnExpr.getNodeInfo();
    if (NodeInfo.Kind.LOCATION == returnExprNodeInfo.getKind()
        && !returnExprNodeInfo.isCoersionApplied()) {
      t.add("ret", PrinterLocation.toString((Location) returnExprNodeInfo.getSource()));
    } else {
      t.add("ret", String.format("new Location(%s)", new PrinterExpr(returnExpr)));
    }
  }

  @Override
  public ST build(STGroup group) {
    final ST t = group.getInstanceOf("mode");

    buildHeader(group, t);
    buildArguments(group, t);
    buildAttributes(group, t);
    buildReturnExpession(t);

    return t;
  }
}
