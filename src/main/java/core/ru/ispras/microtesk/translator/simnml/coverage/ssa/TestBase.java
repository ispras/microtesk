package ru.ispras.microtesk.translator.simnml.coverage.ssa;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintUtils;
import ru.ispras.fortress.solver.xml.XMLConstraintLoader;
import ru.ispras.fortress.solver.xml.XMLNotLoadedException;
import ru.ispras.fortress.transformer.NodeTransformer;

import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.simnml.coverage.ssa.Expression.EQ;

public final class TestBase {
  final String path;
  final Map<String, Map<String, SsaForm>> storage;
  final ru.ispras.testbase.stub.TestBase testBase;

  public TestBase(String path) {
    this.path = path;
    this.storage = new HashMap<>();
    this.testBase = new ru.ispras.testbase.stub.TestBase();
  }

  public TestBase() {
    this(".");
  }

  public TestBaseQueryResult executeQuery(TestBaseQuery query) {
    final TestBaseQueryResult rc = testBase.executeQuery(query);
    if (rc.getStatus() == TestBaseQueryResult.Status.OK) {
      return rc;
    }
    System.err.println("Running custom TestBase engine...");
    final PathConstraintBuilder builder = constraintBuilder(query);
    System.err.println("PathConstraintBuilder has been built.");

    final Collection<Node> bindings = gatherBindings(query, builder.getVariables());
    bindings.add(findPathSpec(query, builder.getVariables()));

    final String name = query.getContext().get(TestBaseContext.TESTCASE);
    bindings.add(EQ(new NodeVariable(name + "!1", DataType.BOOLEAN), Expression.TRUE));

    final Constraint constraint = builder.build(bindings);
    final SolverResult result = ConstraintUtils.solve(constraint);
    System.err.println(constraint);
    System.err.println(result);

    return fromSolverResult(query, result);
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
        } else {
          System.err.printf("Skipping unknown binding: %s, %s\n", entry.getKey(), entry.getValue());
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
    final File dir = new File("gen/" + model);
    final Collection<Constraint> constraints = new ArrayList<>();
    for (File file : dir.listFiles()) {
      try {
        constraints.add(XMLConstraintLoader.loadFromFile(file.getPath()));
      } catch (XMLNotLoadedException e) {
        System.err.println(e.getMessage());
      }
    }
    final SsaConverter converter = new SsaConverter(constraints);
    final Map<String, SsaForm> ssa = new HashMap<>();

    try {
      final BufferedReader reader = new BufferedReader(new FileReader(String.format("gen/%s.list", model)));
      String line = reader.readLine();
      while (line != null) {
        ssa.put(line, converter.convert(line));
        line = reader.readLine();
      }
    } catch (java.io.IOException e) {
      System.err.println(e.getMessage());
    }
    return ssa;
  }
}
