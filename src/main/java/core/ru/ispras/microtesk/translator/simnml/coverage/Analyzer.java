/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.coverage;

import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.xml.XMLConstraintSaver;
import ru.ispras.fortress.solver.xml.XMLNotSavedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class Analyzer {
  private final IR ir;
  private final Map<String, SsaForm> ssa;
  private final String modelName;

  public Analyzer(IR ir, String modelName) {
    if (ir == null) {
      throw new NullPointerException();
    }
    this.ir = ir;
    this.ssa = new TreeMap<>();
    this.modelName = modelName;
  }

  public void run() {
    if (!ssa.isEmpty()) {
      return;
    }
    processModes(ir.getModes().values());
    processPrimitives(ir.getModes().values());
    processPrimitives(ir.getOps().values());

    final List<Constraint> constraints = new ArrayList<>();
    try {
    final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(String.format("gen/%s.list", modelName))));
    for (Map.Entry<String, SsaForm> entry : ssa.entrySet()) {
        out.println(entry.getKey());
      for (Constraint c : BlockConverter.convert(entry.getKey(), entry.getValue().getEntryPoint())) {
        constraints.add(c);
        final XMLConstraintSaver saver = new XMLConstraintSaver(c);
        try {
        final File path = new File("gen/" + modelName);
        path.mkdir();
        saver.saveToFile(String.format("%s/%s.xml", path.getPath(), c.getName()));
        } catch (XMLNotSavedException e) {
          System.err.println(e.getMessage());
        }
      }
    }
    out.flush();
    out.close();
    } catch (java.io.IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private void processPrimitives(Collection<Primitive> primitives) {
    for (Primitive p : primitives) {
      if (!p.isOrRule()) {
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

/**
 * Helper class to sort primitives accordingly to theirs dependencies.
 * Primitive X depends on Y if Y is found in signature of X.
 */
final class DependencyBuilder {
  private final Map<String, Primitive> selected;

  private Set<String> observed;
  private List<Primitive> ordered;

  public DependencyBuilder(Map<String, Primitive> map) {
    this.selected = Collections.unmodifiableMap(map);
    this.observed = null;
    this.ordered = null;
  }

  public void build() {
    if (ordered != null) {
      return;
    }

    observed = new TreeSet<>();
    ordered = new ArrayList<>(selected.size());

    for (Primitive p : selected.values()) {
      collectDependencies(p);
    }

    observed = null;
  }

  private void collectDependencies(Primitive in) {
    if (observed.contains(in.getName())) {
      return;
    }
    // Avoiding infinite recursion in case of cycle dependency
    observed.add(in.getName());

    if (in.isOrRule()) {
      for (Primitive p : ((PrimitiveOR) in).getORs()) {
        collectDependencies(p);
      }
    } else {
      for (Primitive p : ((PrimitiveAND) in).getArguments().values()) {
        if (selected.get(p.getName()) != null) {
          collectDependencies(p);
        }
      }
    }
    ordered.add(in);
  }

  /**
   * Order primitives for sequential processing.
   *
   * @param map Map of named primitives to be ordered.
   * @return Ordered list of input primitives.
   */
  public static List<Primitive> order(Map<String, Primitive> map) {
    final DependencyBuilder deps = new DependencyBuilder(map);
    deps.build();
    return Collections.unmodifiableList(deps.ordered);
  }
}
