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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class Analyzer {
  private final IR ir;

  private Map<String, List<OpInstance>> instances;
  private Map<String, List<SSAForm>> retValues;

  public Analyzer(IR ir) {
    if (ir == null)
      throw new NullPointerException();

    this.ir = ir;
    this.instances = null;
  }

  public void run() {
    if (instances != null)
      return;

    final DataContainer data = new DataContainer(ir);

    data.instantiate();
    data.rearrange();
    data.expand();

    this.instances = data.multimap;
    this.retValues = data.retValues;
  }

  public static final class Binding {
    public final String name;
    public final Primitive type;
    public final Node value;

    private Binding(String name, Primitive type, Node value) {
      this.name = name;
      this.type = type;
      this.value = value;
    }

    public static Binding bindOp(String name, PrimitiveAND op) {
      return new Binding(name, op, null);
    }

    public static Set<String> collectNames(List<Binding> list) {
      final Set<String> set = new TreeSet<String>();
      for (Binding b : list)
        set.add(b.name);
      return set;
    }
  }

  public Constraint createConstraint(String opName, List<Binding> bindings, List<String> pathSpec) {
    final List<OpInstance> candidates = instances.get(opName);
    final Set<String> boundNames = Binding.collectNames(bindings);
    final OpInstance instance = matchBestBinding(candidates, boundNames);

    if (instance == null)
      throw new IllegalArgumentException("Incorrect binding");

    final SSAForm ssa = instance.getAttributes().get("action");

    final Formulas formulas = new Formulas();
    formulas.add(ssa.getExpression());
    formulas.add(instantiateBindings(instance, bindings));
    formulas.add(instantiatePath(instance, pathSpec));

    final ConstraintBuilder builder = new ConstraintBuilder();
    builder.setName(opName);
    builder.setDescription(instance.getName());
    builder.setInnerRep(formulas);

    return builder.build();
  }

  private Node instantiateBindings(OpInstance instance, List<Binding> bindings) {
    return Expression.TRUE;
  }

  private Node instantiatePath(OpInstance instance, List<String> pathSpec) {
    return Expression.TRUE;
  }

  private OpInstance matchBestBinding(List<OpInstance> list, Set<String> bound) {
    OpInstance bestMatch = null;
    int numArgs = Integer.MAX_VALUE;

    for (OpInstance instance : list)
      if (instance.getUnbound().keySet().containsAll(bound)) {
        int numArgsUsed = instance.getUnbound().keySet().size();
        if (numArgsUsed < numArgs) {
          numArgs = numArgsUsed;
          bestMatch = instance;
        }
      }
    return bestMatch;
  }

  public OpInstance instantiateCompound(InstanceSpec spec) {
    return null;
  }

  private static final class DataContainer {
    private List<Primitive> orderedModes;
    private List<Primitive> orderedOps;

    private Map<String, List<OpInstance>> multimap;
    private Map<String, List<SSAForm>> retValues;

    public DataContainer(IR ir) {
      orderedModes = DependencyBuilder.order(ir.getModes());

      final List<Primitive> simple = new ArrayList<Primitive>();
      final List<Primitive> compound = new ArrayList<Primitive>();

      Splitter.split(DependencyBuilder.order(ir.getOps()), simple, compound);
      orderedOps = simple;

      multimap = new HashMap<String, List<OpInstance>>();
      retValues = new TreeMap<String, List<SSAForm>>();
    }

    public void instantiate() {
      for (Primitive p : orderedModes)
        multimap.put(p.getName(), OpInstance.instantiate(p, multimap));

      for (Primitive p : orderedOps)
        multimap.put(p.getName(), OpInstance.instantiate(p, multimap));

      instantiateReturnValues();
    }

    private void instantiateReturnValues() {
      for (Primitive p : orderedModes)
        if (p.isOrRule())
          retValues.put(p.getName(), collectReturnValues(p));
        else
          retValues.put(p.getName(), buildReturnValue(p));

      retValues.values().removeAll(
          Collections.singleton(Collections.emptyList()));
    }

    private List<SSAForm> buildReturnValue(Primitive in) {
      final PrimitiveAND mode = (PrimitiveAND) in;
      if (mode.getReturnExpr() == null)
        return Collections.emptyList();

      return Collections.singletonList(SSABuilder.fromMode(mode));
    }

    private List<SSAForm> collectReturnValues(Primitive in) {
      final PrimitiveOR parent = (PrimitiveOR) in;
      List<SSAForm> list = Collections.emptyList();

      for (Primitive p : parent.getORs())
        list = Utility.appendList(list, retValues.get(p.getName()));

      return list;
    }

    public void rearrange() {
      final Map<String, ArrayList<OpInstance>> rearranged =
          new TreeMap<String, ArrayList<OpInstance>>();

      for (OpInstance instance : multimap.get("instruction")) {
        final String key = instance.getKeyName();
        if (!rearranged.containsKey(key))
          rearranged.put(key, new ArrayList<OpInstance>());

        rearranged.get(key).add(instance);
      }
      multimap = postprocess(rearranged);
    }

    private Map<String, List<OpInstance>> postprocess(Map<String, ArrayList<OpInstance>> map) {
      final Map<String, List<OpInstance>> instances =
          new TreeMap<String, List<OpInstance>>(map);
      for (Map.Entry<String, ArrayList<OpInstance>> entry : map.entrySet()) {
        entry.getValue().trimToSize();
        instances.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
      }
      return instances;
    }

    public void expand() {
      for (Primitive p : orderedOps)
        if (p.isOrRule())
          multimap.put(p.getName(), collectInstances(p));
        else if (!multimap.containsKey(p.getName()))
          multimap.put(p.getName(), propagateInstances(p));

      multimap.values().removeAll(Collections.emptyList());
    }

    private List<OpInstance> propagateInstances(Primitive in) {
      final PrimitiveAND op = (PrimitiveAND) in;
      for (Primitive arg : op.getArguments().values())
        if (arg.getKind() == Primitive.Kind.OP)
          return multimap.get(arg.getName());

      return Collections.emptyList();
    }

    private List<OpInstance> collectInstances(Primitive in) {
      final PrimitiveOR parent = (PrimitiveOR) in;
      final List<OpInstance> list = new ArrayList<OpInstance>();

      for (Primitive p : parent.getORs())
        list.addAll(multimap.get(p.getName()));

      return list;
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
    if (ordered != null)
      return;

    observed = new TreeSet<String>();
    ordered = new ArrayList<Primitive>(selected.size());

    for (Primitive p : selected.values())
      collectDependencies(p);

    observed = null;
  }

  private void collectDependencies(Primitive in) {
    if (observed.contains(in.getName()))
      return;
    // Avoiding infinite recursion in case of cycle dependency
    observed.add(in.getName());

    if (in.isOrRule())
      for (Primitive p : ((PrimitiveOR) in).getORs())
        collectDependencies(p);
    else
      for (Primitive p : ((PrimitiveAND) in).getArguments().values())
        if (selected.get(p.getName()) != null)
          collectDependencies(p);

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


final class Splitter {
  private final List<Primitive> simple;
  private final List<Primitive> compound;

  Splitter(List<Primitive> simple, List<Primitive> compound) {
    this.simple = simple;
    this.compound = compound;
  }

  public static void split(List<Primitive> input, List<Primitive> simple,
      List<Primitive> compound) {
    final Splitter splitter = new Splitter(simple, compound);
    splitter.split(input);
  }

  private void split(List<Primitive> input) {
    for (Primitive p : input)
      if (isCompound(p))
        compound.add(p);
      else
        simple.add(p);
  }

  private boolean isCompound(Primitive p) {
    if (compound.contains(p))
      return true;

    if (p.isOrRule())
      return isCompound((PrimitiveOR) p);

    return isCompound((PrimitiveAND) p);
  }

  private boolean isCompound(PrimitiveAND p) {
    final List<Primitive> callees = gatherCalleeOps(p);
    if (callees.size() > 1)
      return true;

    for (Primitive callee : callees)
      if (isCompound(callee))
        return true;

    return false;
  }

  private boolean isCompound(PrimitiveOR p) {
    for (Primitive child : ((PrimitiveOR) p).getORs())
      if (isCompound(child))
        return true;

    return false;
  }

  private static List<Primitive> gatherCalleeOps(PrimitiveAND container) {
    final Set<String> callees = new TreeSet<String>();
    for (Attribute attribute : container.getAttributes().values()) {
      if (attribute.getKind() != Attribute.Kind.ACTION)
        continue;
      callees.addAll(OpInstance.collectCallees(attribute.getStatements(), null));
    }
    if (callees.isEmpty())
      return Collections.emptyList();

    final List<Primitive> gathered = new ArrayList<Primitive>(callees.size());
    for (String calleeName : callees) {
      final Primitive callee = container.getArguments().get(calleeName);
      if (callee.getKind() == Primitive.Kind.OP)
        gathered.add(callee);
    }
    return gathered;
  }
}
