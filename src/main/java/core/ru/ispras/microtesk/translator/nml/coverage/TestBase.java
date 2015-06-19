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

package ru.ispras.microtesk.translator.nml.coverage;

import static ru.ispras.microtesk.translator.nml.coverage.Expression.EQ;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.NOT;
import static ru.ispras.microtesk.translator.nml.coverage.Expression.OR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.solver.SolverId;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.xml.XMLConstraintLoader;
import ru.ispras.fortress.solver.xml.XMLNotLoadedException;
import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;

public final class TestBase {
  final String path;
  final Map<String, Map<String, SsaForm>> storage;
  final ru.ispras.testbase.stub.TestBase testBase;

  private static SolverId solverId = SolverId.Z3_TEXT;

  public static void setSolverId(final SolverId value) {
    InvariantChecks.checkNotNull(value);
    solverId = value;
    ru.ispras.testbase.stub.TestBase.setSolverId(value);
  }

  public TestBase(String path) {
    this.path = path;
    this.storage = new HashMap<>();
    this.testBase = ru.ispras.testbase.stub.TestBase.get();
  }

  public TestBase() {
    this(System.getenv("MICROTESK_HOME"));
  }

  public TestBaseQueryResult executeQuery(TestBaseQuery query) {
System.out.println("exec " + query.getContext().get(TestBaseContext.TESTCASE));
    final TestBaseQueryResult rc = testBase.executeQuery(query);
    if (rc.getStatus() == TestBaseQueryResult.Status.OK) {
      return rc;
    }
System.out.println("error");
    SolverResult result;
    try {
      final PathConstraintBuilder builder = constraintBuilder(query);

      final Collection<Node> bindings = gatherBindings(query, builder.getVariables());
      bindings.add(findPathSpec(query, builder.getVariables()));

      final String testCase = query.getContext().get(TestBaseContext.TESTCASE);
      if (testCase.equals("normal")) {
        bindings.add(NOT(OR(builder.getSpecialMarks())));
      } else {
        bindings.add(EQ(findGuard(testCase, builder.getVariables()), Expression.TRUE));
      }

      final Constraint constraint = builder.build(bindings);
      result = solverId.getSolver().solve(constraint);
    } catch (Throwable e) {
      return TestBaseQueryResult.reportErrors(Collections.singletonList(e.getMessage()));
    }

    return fromSolverResult(query, result);
  }

  private static Node findGuard(final String testCase,
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

  private static boolean isOrderedSubset(final List<String> parts,
                                         final List<String> path) {
    final Iterator<String> pathIt = path.iterator();
    final Iterator<String> partsIt = parts.iterator();

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

  private Node findPathSpec(TestBaseQuery query, Map<String, NodeVariable> variables) {
    final Map<String, String> context = query.getContext();
    final String name = context.get(TestBaseContext.INSTRUCTION);
    final String situation = context.get(TestBaseContext.TESTCASE);
    final Pair<String, String> pair = Utility.splitOnFirst(situation, '.');

    for (Map.Entry<String, String> entry : context.entrySet()) {
      if (entry.getValue().equals(name)) {
        final String varName = entry.getKey() + pair.second + "!1";
        if (variables.containsKey(varName)) {
          return variables.get(varName);
        }
      }
    }
    return NodeValue.newBoolean(true);
  }

  private TestBaseQueryResult fromSolverResult(TestBaseQuery query, SolverResult result) {
    switch (result.getStatus()) {
    case SAT:
      return TestBaseQueryResult.success(parseResult(query, result));

    case ERROR:
      final List<String> errors = new ArrayList<>();
      for (String error : result.getErrors()) {
        errors.add(error);
      }
      return TestBaseQueryResult.reportErrors(errors);

    default:
    }
    return TestBaseQueryResult.success(TestDataProvider.empty());
  }

  private TestDataProvider parseResult(TestBaseQuery query, SolverResult result) {
    final Map<String, Data> values = new HashMap<>();
    for (Variable var : result.getVariables()) {
      values.put(var.getName(), var.getData());
    }

    final Map<String, Node> valueNodes = new HashMap<>();
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
    final Iterator<TestData> iterator = Collections.singletonList(data).iterator();

    return new TestDataProvider() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public TestData next() {
        return iterator.next();
      }
    };
  }

  private Collection<Node> gatherBindings(TestBaseQuery query, Map<String, NodeVariable> variables) {
    final List<Node> bindings = new ArrayList<>();
    final NodeTransformer caster = new NodeTransformer(IntegerCast.rules());

    for (Map.Entry<String, Node> entry: query.getBindings().entrySet()) {
      if (entry.getValue().getKind() == Node.Kind.VALUE) {
        final String name = entry.getKey() + "!1";
        if (variables.containsKey(name)) {
          final Node binding = EQ(variables.get(name), entry.getValue());
          bindings.add(Utility.transform(binding, caster));
        }
      }
    }
    return bindings;
  }

  private PathConstraintBuilder constraintBuilder(TestBaseQuery query) {
    final Map<String, String> context = query.getContext();
    final String model = context.get(TestBaseContext.PROCESSOR);
    final String instr = context.get(TestBaseContext.INSTRUCTION);
    final SsaAssembler assembler =
        new SsaAssembler(getStorage(model), context, instr);
    return new PathConstraintBuilder(assembler.assemble(instr));
  }

  private Map<String, SsaForm> getStorage(String model) {
    if (storage.containsKey(model)) {
      return storage.get(model);
    }
    final Map<String, SsaForm> ssa = loadStorage(model);
    storage.put(model, ssa);

    return ssa;
  }

  private Map<String, SsaForm> loadStorage(String model) {
    final String dirName = String.format("%s%sgen", this.path, File.separator);
    final File dir = new File(String.format("%s%s%s", dirName, File.separator, model));
    final Collection<Constraint> constraints = new ArrayList<>();

    for (final File file : dir.listFiles()) {
      try {
        constraints.add(XMLConstraintLoader.loadFromFile(file.getPath()));
      } catch (final XMLNotLoadedException e) {
        System.err.println(e.getMessage());
      }
    }

    final SsaConverter converter = new SsaConverter(constraints);
    final Map<String, SsaForm> ssa = new HashMap<>();

    try {
      final BufferedReader reader = new BufferedReader(
          new FileReader(String.format("%s%s%s.list", dirName, File.separator, model)));

      String line = reader.readLine();
      while (line != null) {
        ssa.put(line, converter.convert(line));
        line = reader.readLine();
      }

      reader.close();
    } catch (final IOException e) {
      System.err.println(e.getMessage());
    }
    return ssa;
  }

  public TestBaseRegistry getRegistry() {
    return testBase.getRegistry();
  }
}
