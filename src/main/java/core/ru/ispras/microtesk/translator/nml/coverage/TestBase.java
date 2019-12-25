/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.translator.mir.MirBuilder;
import ru.ispras.microtesk.translator.mir.MirContext;
import ru.ispras.microtesk.translator.mir.MirText;
import ru.ispras.microtesk.utils.StringUtils;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.EmptyIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ru.ispras.microtesk.utils.StringUtils.dotConc;

public final class TestBase {
  private final Path outputDir;
  private final Map<String, Map<String, SsaForm>> storage;
  private final ru.ispras.testbase.TestBase testBase;

  private static TestBase instance = null;
  private static SolverId solverId = SolverId.CVC4_TEXT;

  public static TestBase get() {
    if (null == instance) {
      instance = new TestBase();
    }
    return instance;
  }

  public static void setSolverId(final SolverId value) {
    InvariantChecks.checkNotNull(value);
    solverId = value;
    ru.ispras.testbase.TestBase.setSolverId(value);
  }

  private TestBase(final String path) {
    this.outputDir = Paths.get(path);
    this.storage = new HashMap<>();
    this.testBase = ru.ispras.testbase.TestBase.get();
  }

  private TestBase() {
    this(SysUtils.getHomeDir());
  }

  public TestBaseQueryResult executeQuery(final TestBaseQuery query) {
    final TestBaseQueryResult rc = testBase.executeQuery(query);
    if (rc.getStatus() == TestBaseQueryResult.Status.OK
        || rc.getStatus() == TestBaseQueryResult.Status.ERROR) {
      return rc;
    }

    SolverResult result;
    try {
      final PathConstraintBuilder builder = newPathConstraintBuilder(query);

      final Collection<Node> bindings = gatherBindings(query, builder.getVariables());
      bindings.add(findPathSpec(query, builder.getVariables()));

      final String testCase = (String) query.getContext().get(TestBaseContext.TESTCASE);
      if (testCase.equals("normal")) {
        final List<NodeVariable> marks = builder.getSpecialMarks();
        if (!marks.isEmpty()) {
          bindings.add(Nodes.not(Nodes.or(marks)));
        }
      } else if (!testCase.equals("undefined") && !testCase.equals("unpredicted")) {
        final List<NodeVariable> marks = new ArrayList<>();
        for (final NodeVariable mark : builder.getSpecialMarks()) {
          if (mark.getName().matches(".*\\.undefined(!(\\d+))?$")
              || mark.getName().matches(".*\\.unpredicted(!(\\d+))?$")) {
            marks.add(mark);
          }
        }
        if (!marks.isEmpty()) {
          bindings.add(Nodes.not(Nodes.or(marks)));
        }
        bindings.add(Nodes.eq(findGuard(testCase, builder.getVariables()), Nodes.TRUE));
      } else {
        // unrestrited access to all paths: same as above, but w/o mark filtering
        bindings.add(Nodes.eq(findGuard(testCase, builder.getVariables()), Nodes.TRUE));
      }

      final Constraint constraint = builder.build(bindings);
      result = solverId.getSolver().solve(constraint);
    } catch (final Throwable e) {
      final List<String> errors = new ArrayList<>(rc.getErrors().size() + 1);

      final StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));

      errors.add(sw.getBuffer().toString());
      errors.addAll(rc.getErrors());

      return TestBaseQueryResult.reportErrors(errors);
    }

    return fromSolverResult(query, result);
  }

  private static Node findGuard(
      final String testCase,
      final Map<String, NodeVariable> variables) {
    final Map<String, List<String>> components = new TreeMap<>();

    for (final NodeVariable var : variables.values()) {
      if (var.isType(DataType.BOOLEAN)) {
        final int pos = var.getName().indexOf('!');
        if (pos > 0) {
          final String key = var.getName().substring(0, pos);
          components.put(key, splitInverse(key));
        }
      }
    }

    final List<String> subset = splitInverse(testCase);
    for (final Map.Entry<String, List<String>> entry : components.entrySet()) {
      if (isOrderedSubset(subset, entry.getValue())) {
        final NodeVariable node = variables.get(entry.getKey() + "!1");
        if (node != null) {
          return node;
        }
      }
    }

    throw new IllegalArgumentException(testCase);
  }

  private static boolean isOrderedSubset(
      final List<String> parts,
      final List<String> path) {
    final java.util.Iterator<String> pathIt = path.iterator();
    final java.util.Iterator<String> partsIt = parts.iterator();

    String part = partsIt.next();
    int matches = 0;
    while (pathIt.hasNext()) {
      final String component = pathIt.next();
      if (component.equals(part)) {
        ++matches;
        if (partsIt.hasNext()) {
          part = partsIt.next();
        } else {
          return true;
        }
      }
    }
    return !partsIt.hasNext() && matches > 0;
  }

  private static List<String> splitInverse(final String s) {
    final List<String> tokens = Arrays.asList(s.split("\\."));
    Collections.reverse(tokens);

    return tokens;
  }

  private Node findPathSpec(
      final TestBaseQuery query,
      final Map<String, NodeVariable> variables) {
    final Map<String, Object> context = query.getContext();

    final String name = (String) context.get(TestBaseContext.INSTRUCTION);
    final String situation = (String) context.get(TestBaseContext.TESTCASE);
    final Pair<String, String> pair = StringUtils.splitOnFirst(situation, '.');

    for (final Map.Entry<String, Object> entry : context.entrySet()) {
      if (entry.getValue().equals(name)) {
        final String varName = entry.getKey() + pair.second + "!1";
        if (variables.containsKey(varName)) {
          return variables.get(varName);
        }
      }
    }

    return NodeValue.newBoolean(true);
  }

  private TestBaseQueryResult fromSolverResult(
      final TestBaseQuery query,
      final SolverResult result) {
    switch (result.getStatus()) {
      case SAT:
        return TestBaseQueryResult.success(parseResult(query, result));

      case ERROR:
        return TestBaseQueryResult.reportErrors(result.getErrors());

      default:
        return TestBaseQueryResult.success(EmptyIterator.<TestData>get());
    }
  }

  private Iterator<TestData> parseResult(final TestBaseQuery query, final SolverResult result) {
    final Map<String, Data> values = new HashMap<>();
    for (final Variable variable : result.getVariables()) {
      values.put(variable.getName(), variable.getData());
    }

    final Map<String, Object> valueNodes = new HashMap<>();
    for (Map.Entry<String, Node> entry : query.getBindings().entrySet()) {
      if (entry.getValue().getKind() == Node.Kind.VARIABLE) {
        final String name = entry.getKey() + "!1";
        if (values.containsKey(name)) {
          valueNodes.put(entry.getKey(), new NodeValue(values.get(name)));
        } else {
          if (entry.getValue().isType(DataTypeId.LOGIC_INTEGER)) {
            // TODO: Bit width required.
            // TODO: Randomization.
            valueNodes.put(entry.getKey(), NodeValue.newInteger(0));
          } else {
            // TODO: Bit width required.
            // TODO: Randomization.
          }
        }
      }
    }

    final TestData data = new TestData(valueNodes);
    return new SingleValueIterator<>(data);
  }

  private Collection<Node> gatherBindings(
      final TestBaseQuery query,
      final Map<String, NodeVariable> variables) {
    final List<Node> bindings = new ArrayList<>();
    final NodeTransformer caster = new NodeTransformer(IntegerCast.rules());

    for (Map.Entry<String, Node> entry: query.getBindings().entrySet()) {
      if (entry.getValue().getKind() == Node.Kind.VALUE) {
        final String name = entry.getKey() + "!1";
        if (variables.containsKey(name)) {
          final Node binding = Nodes.eq(variables.get(name), entry.getValue());
          bindings.add(Transformer.transform(binding, caster));
        }
      }
    }

    return bindings;
  }

  private PathConstraintBuilder newPathConstraintBuilder(final TestBaseQuery query) {
    final Map<String, Object> context = query.getContext();

    final String model = (String) context.get(TestBaseContext.PROCESSOR);
    final String instruction = (String) context.get(TestBaseContext.INSTRUCTION);

    final SsaAssembler assembler = new SsaAssembler(getStorage(model));
    final Node formula = assembler.assemble(context, instruction);

    return new PathConstraintBuilder(formula);
  }

  private static final class MirLinker {
    final Map<String, Object> context;
    final Map<String, Node> bindings;
    final String rootInsn;
    final Map<String, Map<String, String>> images;

    final MirBuilder builder = new MirBuilder();
    final List<Node> args = new java.util.ArrayList<>();
    final Map<String, Integer> observedArgs = new java.util.HashMap<>();

    MirLinker(final TestBaseQuery query) {
      this.context = query.getContext();
      this.bindings = query.getBindings();
      this.rootInsn = context.get(TestBaseContext.INSTRUCTION).toString();
      this.images = parseHierarchy(rootInsn, context);
    }

    static String toString(final Map<String, ? extends Object> map) {
      final StringBuilder sb = new StringBuilder();
      sb.append("{");
      for (final String key : map.keySet()) {
        sb.append(" '")
          .append(key)
          .append("' : '")
          .append(map.get(key).toString())
          .append("' |");
      }
      sb.append("}");
      return sb.toString();
    }

    void buildItem(final String prefix) {
      final Map<String, String> params = images.get(prefix);
      for (final String key : params.keySet()) {
        buildItem(dotConc(prefix, key));
      }
      final String type = context.get(prefix).toString();
      if (!type.equals("#IMM")) {
        builder.makeClosure(type, params.size());
      } else {
        refBoundVar(prefix);
      }
    }

    void linkBindings(final Map<String, Node> bindings) {
      for (final String key : bindings.keySet()) {
        if (!images.get(key).isEmpty()) {
          buildItem(key);
          refBoundVar(key);

          final String callee = dotConc(context.get(key).toString(), "write");
          builder.makeCall(callee, 1);
        }
      }
    }

    void refBoundVar(final String key) {
      final int index;
      if (observedArgs.containsKey(key)) {
        index = observedArgs.get(key);
      } else {
        final Node node = bindings.get(key);
        args.add(node);

        index = builder.addParameter(node.getDataType().getSize());
        observedArgs.put(key, index);
      }
      builder.refParameter(index);
    }

    MirContext build() {
      builder.makeCall(dotConc(rootInsn, "action"), 0);
      return builder.build("");
    }

    static MirContext newMir(final TestBaseQuery query) {
      final MirLinker linker = new MirLinker(query);
      linker.buildItem(linker.rootInsn);
      linker.linkBindings(linker.bindings);

      return linker.build();
    }
  }

  private static Map<String, Map<String, String>> parseHierarchy(
      final String entry, final Map<String, Object> ctx) {
    final List<String> queue = new java.util.ArrayList<>();
    queue.add(entry);

    final Map<String, Map<String, String>> images = new java.util.HashMap<>();
    while (!queue.isEmpty()) {
      final String prefix = removeLast(queue);
      final Map<String, String> args = getArguments(prefix, ctx);
      images.put(prefix, args);

      for (final String key : args.keySet()) {
        queue.add(dotConc(prefix, key));
      }
    }
    return images;
  }

  private static Map<String, String> getArguments(
      final String prefix, final Map<String, Object> ctx) {
    final Map<String, String> args = new java.util.LinkedHashMap<>();
    for (final String key : ctx.keySet()) {
      if (key.startsWith(prefix) && key.lastIndexOf('.') == prefix.length()) {
        final String name = key.substring(prefix.length() + 1);
        args.put(name, ctx.get(key).toString());
      }
    }
    if (args.isEmpty()) {
      return Collections.emptyMap();
    }
    return args;
  }

  private static <T> T removeLast(final List<T> list) {
    return list.remove(list.size() - 1);
  }

  public Map<String, SsaForm> getStorage(final String model) {
    if (storage.containsKey(model)) {
      return storage.get(model);
    }

    final Map<String, SsaForm> ssa = SsaStorage.load(outputDir, model);
    storage.put(model, ssa);

    return ssa;
  }

  public TestBaseRegistry getRegistry() {
    return testBase.getRegistry();
  }
}
