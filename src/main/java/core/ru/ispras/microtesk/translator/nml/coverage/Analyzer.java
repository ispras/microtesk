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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.analysis.IrInquirer;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class Analyzer implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private Ir ir;
  private IrInquirer inquirer;
  private Map<String, SsaForm> ssa;

  public Analyzer(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    this.ir = ir;
    this.inquirer = new IrInquirer(ir);
    this.ssa = new TreeMap<>();

    generateOutput(translator.getOutDir());
  }

  private void generateOutput(final String targetDir) {
    InvariantChecks.checkTrue(ssa.isEmpty());
    InvariantChecks.checkNotNull(targetDir);

    processModes(ir.getModes().values());
    processPrimitives(ir.getModes().values());
    processPrimitives(ir.getOps().values());

    final String modelName = ir.getModelName();

    SsaStorage.store(targetDir, modelName, ssa);
  }

  private void processPrimitives(Collection<Primitive> primitives) {
    for (Primitive p : primitives) {
      if (!p.isOrRule()) {
        processParameters((PrimitiveAND) p);
        processAttributes((PrimitiveAND) p);
      }
    }
  }

  private void processAttributes(PrimitiveAND op) {
    for (Attribute a : op.getAttributes().values()) {
      if (a.getKind() == Attribute.Kind.ACTION) {
        final SsaBuilder builder =
            new SsaBuilder(inquirer, op.getName(), a.getName(), a.getStatements());
        ssa.put(Utility.dotConc(op.getName(), a.getName()), builder.build());
      }
    }
  }

  private void processParameters(PrimitiveAND op) {
    ssa.put(op.getName() + ".parameters", SsaBuilder.parametersList(inquirer, op));
  }

  private void processModes(Collection<Primitive> modes) {
    for (Primitive p : modes) {
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
