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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.xml.XMLConstraintSaver;
import ru.ispras.fortress.solver.xml.XMLNotSavedException;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.nml.ir.IR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.utils.FileUtils;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class Analyzer {
  private final IR ir;
  private final Map<String, SsaForm> ssa;
  private final String modelName;

  public Analyzer(final IR ir, final String modelName) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(modelName);

    this.ir = ir;
    this.ssa = new TreeMap<>();
    this.modelName = modelName;
  }

  public void generateOutput(final String targetDir) {
    InvariantChecks.checkTrue(ssa.isEmpty());
    InvariantChecks.checkNotNull(targetDir);

    processModes(ir.getModes().values());
    processPrimitives(ir.getModes().values());
    processPrimitives(ir.getOps().values());

    final List<Constraint> constraints = new ArrayList<>();

    try {
      final File genDir = new File(FileUtils.getNormalizedPath(targetDir));

      if (!genDir.exists()) {
        genDir.mkdir();
      }

      final PrintWriter out = new PrintWriter(
          new BufferedWriter(new FileWriter(
              String.format("%s%s%s.list", genDir.getCanonicalPath(), File.separator, modelName))));

      for (final Map.Entry<String, SsaForm> entry : ssa.entrySet()) {
        out.println(entry.getKey());
        for (final Constraint c :
          BlockConverter.convert(entry.getKey(), entry.getValue().getEntryPoint())) {
          constraints.add(c);

          final XMLConstraintSaver saver = new XMLConstraintSaver(c);

          try {
            final File path = new File(
                String.format("%s%s%s", genDir.getCanonicalPath(), File.separator, modelName));

            path.mkdir();
            saver.saveToFile(
                String.format("%s%s%s.xml", path.getPath(), File.separator, c.getName()));
          } catch (final XMLNotSavedException e) {
            Logger.error("Failed to save coverage model to the XML file: %s", e.getMessage());
          }
        }
      }

      out.flush();
      out.close();
    } catch (final java.io.IOException e) {
      Logger.error("Failed to save coverage model to the file system: %s", e.getMessage());
    }
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
        final SsaBuilder builder = new SsaBuilder(op.getName(), a.getStatements());
        final String name = String.format("%s.%s", op.getName(), a.getName());
        ssa.put(name, builder.build());
      }
    }
  }

  private void processParameters(PrimitiveAND op) {
    ssa.put(op.getName() + ".parameters", SsaBuilder.parametersList(op));
  }

  private void processModes(Collection<Primitive> modes) {
    for (Primitive p : modes) {
      if (!p.isOrRule() && p.getReturnType() != null) {
        final PrimitiveAND mode = (PrimitiveAND) p;
        ssa.put(mode.getName() + ".expand",
                SsaBuilder.macroExpansion(mode.getName(), mode.getReturnExpr()));
        ssa.put(mode.getName() + ".update",
                SsaBuilder.macroUpdate(mode.getName(), mode.getReturnExpr()));
      }
    }
  }
}
