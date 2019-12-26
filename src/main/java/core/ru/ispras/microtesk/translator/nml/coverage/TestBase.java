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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.translator.mir.Mir2Node;
import ru.ispras.microtesk.translator.mir.MirArchive;
import ru.ispras.microtesk.translator.mir.MirBuilder;
import ru.ispras.microtesk.translator.mir.MirContext;
import ru.ispras.microtesk.translator.mir.MirPassDriver;
import ru.ispras.microtesk.translator.mir.MirText;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.ispras.microtesk.utils.StringUtils.dotConc;

public final class TestBase {
  private final Path outputDir;
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

    try {
      final String testCase = (String) query.getContext().get(TestBaseContext.TESTCASE);
      final MirInvoke invoke = buildMir(query);
      final Constraint c = invoke.newConstraint(testCase);
      final SolverResult result = solverId.getSolver().solve(c);
      return forwardResult(query, result);
    } catch (final Throwable e) {
      final List<String> errors =
          new java.util.ArrayList<>(rc.getErrors().size() + 1);
      final StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));

      errors.add(sw.getBuffer().toString());
      errors.addAll(rc.getErrors());

      return TestBaseQueryResult.reportErrors(errors);
    }
  }

  private static MirInvoke buildMir(final TestBaseQuery query) {
    final String modelName =
        (String) query.getContext().get(TestBaseContext.PROCESSOR);
    final Path path = Paths.get(SysUtils.getHomeDir(), "gen", modelName + ".zip");
    final MirArchive archive = MirArchive.open(path);

    final MirInvoke invoke = MirLinker.invoke(query);
    final MirPassDriver mirc =
        MirPassDriver.newOptimizing().setStorage(archive.loadAll());

    return new MirInvoke(mirc.apply(invoke.mir), invoke.args);
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

  private static TestBaseQueryResult forwardResult(
      final TestBaseQuery query, final SolverResult result) {
    switch (result.getStatus()) {
    default:
      return TestBaseQueryResult.success(EmptyIterator.<TestData>get());

    case ERROR:
      return TestBaseQueryResult.reportErrors(result.getErrors());

    case SAT:
      final Map<String, Data> values = valueMap(result.getVariables());
      values.keySet().retainAll(query.getBindings().keySet());
      final Map<String, Object> valuesOpaque =
          new java.util.HashMap<>(values.size());
      for (final Map.Entry<String, Data> entry : values.entrySet()) {
        valuesOpaque.put(entry.getKey(), new NodeValue(entry.getValue()));
      }
      return TestBaseQueryResult.success(
          new SingleValueIterator<>(new TestData(valuesOpaque)));
    }
  }

  static Map<String, Data> valueMap(final Collection<Variable> vars) {
    final Map<String, Data> values = new java.util.HashMap<>();
    for (final Variable v : vars) {
      values.put(v.getName(), v.getData());
    }
    return values;
  }

  private static String toString(final Map<String, ? extends Object> map) {
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

  private static String listToLines(final List<?> list) {
    final StringBuilder sb = new StringBuilder();
    for (final Object item : list) {
      sb.append(item.toString()).append(System.lineSeparator());
    }
    return sb.toString();
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

    static MirInvoke invoke(final TestBaseQuery query) {
      final MirLinker linker = new MirLinker(query);
      linker.buildItem(linker.rootInsn);
      linker.linkBindings(linker.bindings);
      linker.builder.makeCall(dotConc(linker.rootInsn, "action"), 0);

      return new MirInvoke(linker.builder.build(""), linker.args);
    }
  }

  private static final class MirInvoke {
    final MirContext mir;
    final List<Node> args;

    MirInvoke(final MirContext mir, final List<Node> args) {
      this.mir = mir;
      this.args = args;
    }

    Constraint newConstraint(final String qualifier) {
      final List<Node> nodes = bindArguments(this.args);
      nodes.addAll(asNodes(mir));
      collectConstraints(qualifier, nodes, nodes);
      return ConstraintUtils.newConstraint(nodes);
    }

    static void collectConstraints(
        final String qual, final List<Node> nodes, final List<Node> constraints) {
      final List<NodeVariable> qualifiers = collectPathQualifiers(nodes);
      initQualifiers(qualifiers, constraints);

      if (qual.equals("normal")) {
        final Pattern p = Pattern.compile("^\\$mark_.*");
        final List<NodeVariable> relevant = new java.util.ArrayList<>(qualifiers);
        relevant.removeAll(filter(qualifiers, p));

        final Node zeroBit = NodeValue.newBitVector(0, 1);
        for (final NodeVariable node : relevant) {
          constraints.add(Nodes.eq(node, zeroBit));
        }
      } else {
        final Pattern p = Pattern.compile(
            String.format("^\\$.*%s.*", Pattern.quote(qual)));
        final List<NodeVariable> relevant = filter(qualifiers, p);
        if (relevant.isEmpty()) {
          Logger.error("TestBase: invalid path qualifier '%s', allowed values are substrings of:%s%s",
            qual,
            System.lineSeparator(),
            listToLines(qualifiers).replaceAll("\\$|!\\d+", ""));
          throw new IllegalArgumentException(
              String.format("Invalid path qualifier '%s'", qual));
        }

        final Node oneBit = NodeValue.newBitVector(1, 1);
        final List<Node> bound = new java.util.ArrayList<>();
        for (final NodeVariable node : relevant) {
          bound.add(Nodes.eq(node, oneBit));
        }
        if (bound.size() > 1) {
          constraints.add(Nodes.or(bound));
        } else {
          constraints.addAll(bound);
        }
      }
    }

    static void initQualifiers(
        final List<NodeVariable> qualifiers, final List<Node> constraints) {
      final Pattern verp = Pattern.compile("!\\d+$");
      final Node zeroBit = NodeValue.newBitVector(0, 1);

      for (final NodeVariable node : qualifiers) {
        final String name = verp.matcher(node.getName()).replaceFirst("!1");
        constraints.add(Nodes.eq(NodeVariable.newBitVector(name, 1), zeroBit));
      }
    }

    static List<NodeVariable> filter(
        final Collection<NodeVariable> vars, final Pattern p) {
      final List<NodeVariable> filtered = new java.util.ArrayList<>();
      for (final NodeVariable node : vars) {
        if (p.matcher(node.getName()).matches()) {
          filtered.add(node);
        }
      }
      return filtered;
    }

    static List<NodeVariable> collectPathQualifiers(
        final Collection<? extends Node> nodes) {
      final Pattern hiddenName = Pattern.compile("^\\$(\\w+)!\\d+");
      final Map<String, NodeVariable> qualifiers = new java.util.HashMap<>();
      final ExprTreeWalker walker = new ExprTreeWalker(new ExprTreeVisitorDefault() {
        @Override
        public void onOperationBegin(final NodeOperation node) {
          if (ExprUtils.isOperation(node, StandardOperation.EQ)
              && ExprUtils.isVariable(node.getOperand(0))) {
            final NodeVariable v = (NodeVariable) node.getOperand(0);
            final Matcher m = hiddenName.matcher(v.getName());
            if (m.matches()) {
              qualifiers.put(m.group(1), v); // last wins the spot
            }
          }
        }
      });
      walker.visit(nodes);
      return new java.util.ArrayList<>(qualifiers.values());
    }

    static List<Node> bindArguments(final List<Node> args) {
      final List<Node> bound = new java.util.ArrayList<>();
      for (int i = 0; i < args.size(); ++i) {
        final Node arg = args.get(i);
        final Node var = NodeVariable.newBitVector(
            String.format("%%%d", i + 1), arg.getDataType().getSize());
        bound.add(Nodes.eq(var, arg));
      }
      return bound;
    }

    static List<Node> asNodes(final MirContext mir) {
      final Mir2Node pass = new Mir2Node();
      pass.apply(mir);
      return pass.getFormulae();
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

  public TestBaseRegistry getRegistry() {
    return testBase.getRegistry();
  }
}
