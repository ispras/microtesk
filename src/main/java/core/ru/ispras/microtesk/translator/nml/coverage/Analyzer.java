/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.analysis.IrInquirer;
import ru.ispras.microtesk.translator.nml.ir.expr.TypeCast;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class Analyzer implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private IrInquirer inquirer;
  private Map<String, SsaForm> ssa;

  public Analyzer(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    this.inquirer = new IrInquirer(ir);
    this.ssa = new TreeMap<>();

    processModes(ir.getModes().values());
    processPrimitives(ir.getModes().values());
    processPrimitives(ir.getOps().values());

    SsaStorage.store(translator.getOutDir(), ir.getModelName(), ssa);
  }

  private void processPrimitives(final Collection<Primitive> primitives) {
    for (final Primitive p : primitives) {
      if (!p.isOrRule()) {
        processParameters((PrimitiveAND) p);
        processAttributes((PrimitiveAND) p);
      }
    }
  }

  private void processAttributes(final PrimitiveAND op) {
    for (final Attribute a : op.getAttributes().values()) {
      if (a.getKind() == Attribute.Kind.ACTION) {
        final SsaBuilder builder =
            new SsaBuilder(inquirer, op.getName(), a.getName(), a.getStatements());
        ssa.put(Utility.dotConc(op.getName(), a.getName()), builder.build());
      }
    }
  }

  private void processParameters(final PrimitiveAND op) {
    ssa.put(op.getName() + ".parameters", newParametersList(inquirer, op));
  }

  private static SsaForm newParametersList(final IrInquirer inquirer, final PrimitiveAND p) {
    final SsaBuilder builder = new SsaBuilder(inquirer, p.getName());
    builder.acquireBlockBuilder();

    final List<Node> parameters = new ArrayList<>(p.getArguments().size());
    for (final Map.Entry<String, Primitive> entry : p.getArguments().entrySet()) {
      final DataType type = convertType(entry.getValue().getReturnType());
      parameters.add(new NodeVariable(entry.getKey(), type));
    }
    builder.addToContext(new NodeOperation(SsaOperation.PARAMETERS, parameters));

    return builder.build();
  }

  private static DataType convertType(final Type type) {
    if (type == null) {
      return DataType.BOOLEAN;
    }
    return TypeCast.getFortressDataType(type);
  }

  private void processModes(final Collection<Primitive> modes) {
    for (final Primitive p : modes) {
      if (!p.isOrRule() && p.getReturnType() != null) {
        final PrimitiveAND mode = (PrimitiveAND) p;
        ssa.put(mode.getName() + ".expand",
                SsaBuilder.macroExpansion(inquirer, mode.getName(), mode.getReturnExpr()));
        ssa.put(mode.getName() + ".update",
                SsaBuilder.macroUpdate(inquirer, mode.getName(), mode.getReturnExpr()));
      }
    }
  }
}
