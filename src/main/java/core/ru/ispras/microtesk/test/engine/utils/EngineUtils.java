/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.utils;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;
import static ru.ispras.fortress.util.InvariantChecks.checkFalse;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.InstructionCall;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.IsaPrimitiveBuilder;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.settings.ExtensionSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.engine.EngineConfig;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.InitializerMaker;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.Stream;
import ru.ispras.microtesk.test.template.StreamStore;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryBuilder;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestBaseRegistry;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * {@link EngineUtils} implements functions shared among test data generators.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class EngineUtils {
  public static final TestData NO_TEST_DATA = new TestData(Collections.<String, Object>emptyMap());

  private EngineUtils() {}

  public static TestBase newTestBase(final GeneratorSettings settings) {
    final TestBase testBase = TestBase.get();
    final TestBaseRegistry registry = testBase.getRegistry();

    if (null == settings || null == settings.getExtensions()) {
      return testBase;
    }

    // Register the user-defined test data generators.
    final String home = SysUtils.getHomeDir();
    final File file = new File(new File(new File(new File(home), "lib"), "jars"), "models.jar");

    final URL url;
    try {
      url = file.toURI().toURL();
    } catch (MalformedURLException e1) {
      Logger.error(e1.getMessage());
      return testBase;
    }

    final URL[] urls = new URL[]{url};
    final ClassLoader loader = new URLClassLoader(urls);

    for (final ExtensionSettings ext : settings.getExtensions().getExtensions()) {
      try {
        final Class<?> cls = loader.loadClass(ext.getPath());
        final DataGenerator generator = DataGenerator.class.cast(cls.newInstance());
        registry.registerGenerator(ext.getName(), generator);
      } catch (final Exception e) {
        Logger.error(e.getMessage());
        e.printStackTrace();
      }
    }

    return testBase;
  }

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final Primitive primitive,
      final Situation situation,
      final Set<AddressingModeWrapper> initializedModes) throws ConfigurationException {
    return makeInitializer(engineContext, primitive, situation, initializedModes, null);
  }

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final Primitive primitive,
      final Situation situation,
      final Set<AddressingModeWrapper> initializedModes,
      final IsaPrimitive concretePrimitive) throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(primitive);
    checkNotNull(initializedModes);
    // Parameter {@code situation} can be null.
    // Parameter {@code concretePrimitive} can be null.

    final TestBaseQueryCreator queryCreator =
        new TestBaseQueryCreator(engineContext, situation, primitive);

    final TestData testData = getTestData(engineContext, primitive, situation, queryCreator);
    Logger.debug(testData.toString());
    situation.getAttributes().put("TestData", testData);

    if (testData != NO_TEST_DATA) {
      setUnknownImmValues(
          queryCreator.getUnknownImmValues(),
          testData,
          null != concretePrimitive ? concretePrimitive.getArguments() : null
          );
    }

    final InitializerMaker initializerMaker = 
        EngineConfig.get().getInitializerMaker(testData.getId());

    InvariantChecks.checkNotNull(
        initializerMaker, "Initializer maker is undefined for " + testData.getId());

    return initializerMaker.makeInitializer(
        engineContext,
        primitive,
        situation,
        testData,
        queryCreator.getModes(),
        initializedModes
        );
  }

  private static TestData getDefaultTestData(
      final EngineContext engineContext,
      final Primitive primitive,
      final TestBaseQueryCreator queryCreator) {
    checkNotNull(engineContext);
    checkNotNull(primitive);
    checkNotNull(queryCreator);

    final Map<String, Argument> args = new HashMap<>();
    args.putAll(queryCreator.getUnknownImmValues());
    args.putAll(queryCreator.getModes());

    final Map<String, Object> bindings = new HashMap<>();
    for (final Map.Entry<String, Argument> entry : args.entrySet()) {
      final String name = entry.getKey();
      final Argument arg = entry.getValue();

      if (arg.getMode() == ArgumentMode.IN || arg.getMode() == ArgumentMode.INOUT) {
        if (arg.getKind() == Argument.Kind.MODE) {
          try {
            final IsaPrimitive concreteMode = makeMode(engineContext, arg);
            final Model model = engineContext.getModel();
            if (concreteMode.access(model.getPE(), model.getTempVars()).isInitialized()) {
              continue;
            }
          } catch (ConfigurationException e) {
            Logger.error(e.getMessage());
          }
        }

        final BitVector value = BitVector.newEmpty(arg.getType().getBitSize());
        Randomizer.get().fill(value);

        bindings.put(name, NodeValue.newBitVector(value));
      }
    }

    return new TestData(bindings);
  }

  public static TestData getTestData(
      final EngineContext engineContext,
      final Primitive primitive,
      final Situation situation,
      final TestBaseQueryCreator queryCreator) {
    checkNotNull(engineContext);
    checkNotNull(primitive);
    checkNotNull(queryCreator);

    Logger.debug("Processing situation %s for %s...", situation, primitive.getSignature());
    if (situation == null) {
      return engineContext.getOptions().getValueAsBoolean(Option.DEFAULT_TEST_DATA) ?
          getDefaultTestData(engineContext, primitive, queryCreator) : NO_TEST_DATA;
    }

    final TestBaseQuery query = queryCreator.getQuery();
    Logger.debug("Query to TestBase: " + query);

    final Map<String, Argument> unknownImmediateValues = queryCreator.getUnknownImmValues();
    Logger.debug("Unknown immediate values: " + unknownImmediateValues.keySet());

    final Map<String, Argument> modes = queryCreator.getModes();
    Logger.debug("Modes used as arguments: " + modes);

    final TestBase testBase = engineContext.getTestBase();
    final TestBaseQueryResult queryResult = testBase.executeQuery(query);

    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      Logger.warning(makeErrorMessage(queryResult) + ": default test data will be used");
      return getDefaultTestData(engineContext, primitive, queryCreator);
    }

    final java.util.Iterator<TestData> dataProvider = queryResult.getDataProvider();
    if (!dataProvider.hasNext()) {
      Logger.warning("No data was generated for the query: default test data will be used");
      return getDefaultTestData(engineContext, primitive, queryCreator);
    }

    return dataProvider.next();
  }

  public static void setUnknownImmValue(
      final Argument argument,
      final Node value,
      final Immediate argumentToPatch) {
    checkNotNull(argument);
    checkNotNull(value);
    checkTrue(value.getKind() == Node.Kind.VALUE);

    final Data data = ((NodeValue) value).getData();
    final BigInteger dataValue;

    if (data.isType(DataTypeId.LOGIC_INTEGER)) {
      dataValue = data.getInteger();
    } else if (data.isType(DataTypeId.BIT_VECTOR)) {
      dataValue = data.getBitVector().bigIntegerValue();
    } else {
      throw new IllegalStateException(String.format("%s cannot be converted to integer", value));
    }

    final UnknownImmediateValue target = (UnknownImmediateValue) argument.getValue();
    target.setValue(dataValue);

    if (null != argumentToPatch) {
      argumentToPatch.access().setValue(dataValue);
    }
  }

  public static void setUnknownImmValues(
      final Map<String, Argument> unknownImmValues,
      final TestData testData) {
    setUnknownImmValues(unknownImmValues, testData, null);
  }

  public static void setUnknownImmValues(
      final Map<String, Argument> unknownImmValues,
      final TestData testData,
      final Map<String, IsaPrimitive> argumentsToPatch) {
    checkNotNull(unknownImmValues);
    checkNotNull(testData);

    for (final Map.Entry<String, Argument> e : unknownImmValues.entrySet()) {
      final Argument argument = e.getValue();
      final Node value = (Node) testData.getBindings().get(e.getKey());

      final Immediate argumentToPatch;
      if (null != argumentsToPatch) {
        argumentToPatch = (Immediate) argumentsToPatch.get(argument.getName());
        checkNotNull(argumentToPatch);
      } else {
        argumentToPatch = null;
      }

      setUnknownImmValue(argument, value, argumentToPatch);
    }
  }

  public static String getSituationName(final AbstractCall abstractCall) {
    checkNotNull(abstractCall);

    final Primitive primitive = abstractCall.getRootOperation();
    if (primitive == null) {
      return null;
    }

    final Situation situation = primitive.getSituation();
    if (situation == null) {
      return null;
    }

    return situation.getName();
  }

  private static List<LabelReference> labelRefs = null; 

  public static List<ConcreteCall> makeConcreteCalls(
      final EngineContext engineContext,
      final List<AbstractCall> abstractSequence) throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(abstractSequence);

    final List<ConcreteCall> concreteSequence = new ArrayList<>();

    for (final AbstractCall abstractCall : abstractSequence) {
      final ConcreteCall concreteCall = makeConcreteCall(engineContext, abstractCall);
      concreteSequence.add(concreteCall);
    }

    return concreteSequence;
  }

  public static ConcreteCall makeConcreteCall(
      final EngineContext engineContext,
      final AbstractCall abstractCall) throws ConfigurationException {
    checkNotNull(engineContext);
    checkNotNull(abstractCall);

    // A preparator call must be expanded when the preparator containing this call is instantiated.
    checkFalse(abstractCall.isPreparatorCall() , "Unexpanded preparator invocation.");

    if (!abstractCall.isExecutable()) {
      return new ConcreteCall(abstractCall);
    }

    try {
      labelRefs = new ArrayList<>();

      final Primitive rootOp = abstractCall.getRootOperation();
      checkRootOp(rootOp);

      final IsaPrimitive op = makeOp(engineContext, rootOp);
      final InstructionCall executable = engineContext.getModel().newCall(op);

      return new ConcreteCall(abstractCall, executable, labelRefs);
    } finally {
      labelRefs = null;
    }
  }

  public static ConcreteCall makeSpecialConcreteCall(
      final EngineContext engineContext,
      final String instructionName) {
    checkNotNull(engineContext);
    checkNotNull(instructionName);

    final Model model = engineContext.getModel();
    final IsaPrimitive operation;
    try {
      final IsaPrimitiveBuilder operationBuilder = model.newOp(instructionName, null);
      operation = operationBuilder.build();
    } catch (final ConfigurationException e) {
      return null;
    }

    final InstructionCall executable = model.newCall(operation);
    return new ConcreteCall(executable);
  }

  public static BigInteger makeImm(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM);
    return (BigInteger) argument.getValue();
  }

  public static BigInteger makeImmRandom(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_RANDOM);
    return ((RandomValue) argument.getValue()).getValue();
  }

  public static BigInteger makeImmUnknown(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_UNKNOWN);
    return ((UnknownImmediateValue) argument.getValue()).getValue();
  }

  public static BigInteger makeImmLazy(final Argument argument) {
    checkArgKind(argument, Argument.Kind.IMM_LAZY);
    return ((LazyValue) argument.getValue()).getValue();
  }

  public static BigInteger makeLabel(final Argument argument) {
    checkArgKind(argument, Argument.Kind.LABEL);
    return ((LabelValue) argument.getValue()).getValue();
  }

  public static IsaPrimitive makeMode(
      final EngineContext engineContext,
      final Argument argument) throws ConfigurationException {
    checkNotNull(engineContext);
    checkArgKind(argument, Argument.Kind.MODE);

    final Primitive abstractMode = (Primitive) argument.getValue();
    return makeMode(engineContext, abstractMode);
  }

  public static IsaPrimitive makeMode(
      final EngineContext engineContext,
      final Primitive abstractMode) throws ConfigurationException {
    checkNotNull(engineContext);
    checkMode(abstractMode);

    final IsaPrimitiveBuilder builder =
        engineContext.getModel().newMode(abstractMode.getName());

    for (Argument arg : abstractMode.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgument(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgument(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgument(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgument(argName, makeImmLazy(arg));
          break;

        case LABEL:
          builder.setArgument(argName, makeLabel(arg));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.build();
  }

  public static IsaPrimitive makeOp(
      final EngineContext engineContext,
      final Argument argument) throws ConfigurationException {
    checkNotNull(engineContext);
    checkArgKind(argument, Argument.Kind.OP);

    final Primitive abstractOp = (Primitive) argument.getValue();
    return makeOp(engineContext, abstractOp);
  }

  public static IsaPrimitive makeOp(
      final EngineContext engineContext,
      final Primitive abstractOp) throws ConfigurationException {
    checkNotNull(engineContext);
    checkOp(abstractOp);

    final String name = abstractOp.getName();
    final String context = abstractOp.getContextName();

    final IsaPrimitiveBuilder builder =
        engineContext.getModel().newOp(name, context);

    for (Argument arg : abstractOp.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
          builder.setArgument(argName, makeImm(arg));
          break;

        case IMM_RANDOM:
          builder.setArgument(argName, makeImmRandom(arg));
          break;

        case IMM_UNKNOWN:
          builder.setArgument(argName, makeImmUnknown(arg));
          break;

        case IMM_LAZY:
          builder.setArgument(argName, makeImmLazy(arg));
          break;

        case LABEL:
          //builder.setArgument(argName, makeLabel(arg));
          labelRefs.add(new LabelReference(
              (LabelValue) arg.getValue(),
              builder.setArgument(argName, makeLabel(arg))
              ));
          break;

        case MODE:
          builder.setArgument(argName, makeMode(engineContext, arg));
          break;

        case OP:
          builder.setArgument(argName, makeOp(engineContext, arg));
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.build();
  }

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final Primitive mode,
      final BitVector value) {
    checkNotNull(engineContext);
    checkNotNull(mode);
    checkNotNull(value);

    final PreparatorStore preparators = engineContext.getPreparators();
    final Preparator preparator = preparators.getPreparator(mode, value, null);

    if (null != preparator) {
      return preparator.makeInitializer(preparators, mode, value, null);
    }

    throw new GenerationAbortedException(
        String.format("No suitable preparator is found for %s.", mode.getSignature()));
  }

  public static List<AbstractCall> makeStreamInit(
      final EngineContext engineContext, final String streamId) {
    checkNotNull(engineContext);
    checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getInit();
  }

  public static List<AbstractCall> makeStreamRead(
      final EngineContext engineContext, final String streamId) {
    checkNotNull(engineContext);
    checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getRead();
  }

  public static List<AbstractCall> makeStreamWrite(
      final EngineContext engineContext, final String streamId) {
    checkNotNull(engineContext);
    checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getWrite();
  }

  public static String makeErrorMessage(final TestBaseQueryResult queryResult) {
    checkNotNull(queryResult);

    final StringBuilder sb = new StringBuilder(String.format(
      "Failed to execute the query. Status: %s.", queryResult.getStatus()));

    if (!queryResult.hasErrors()) {
      return sb.toString();
    }
    sb.append(" Errors: ");
    for (String error : queryResult.getErrors()) {
      sb.append(System.lineSeparator() + "  " + error);
    }

    return sb.toString();
  }

  public static void acquireContext(
      final TestBaseQueryBuilder builder,
      final String prefix,
      final Primitive p) {
    checkNotNull(builder);
    checkNotNull(prefix);
    checkNotNull(p);

    for (final Argument arg : p.getArguments().values()) {
      final String ctxArgName = (prefix.isEmpty())
                                ? arg.getName()
                                : prefix + "." + arg.getName();
      builder.setContextAttribute(ctxArgName, arg.getTypeName());
      switch (arg.getKind()) {
      case OP:
      case MODE:
        acquireContext(builder, ctxArgName, (Primitive) arg.getValue());
        break;

      default:
      }
    }
  }

  public static Set<AddressingModeWrapper> getOutAddressingModes(final List<AbstractCall> calls) {
    checkNotNull(calls);

    final Set<AddressingModeWrapper> modes = new LinkedHashSet<>();
    for (final AbstractCall call : calls) {
      if (call.isExecutable()) {
        saveOutAddressingModes(call.getRootOperation(), modes);
      }
    }
    return modes;
  }

  private static void saveOutAddressingModes(
      final Primitive root,
      final Set<AddressingModeWrapper> modes) {
    for (final Argument argument : root.getArguments().values()) {
      if (Argument.Kind.MODE != argument.getKind() && 
          Argument.Kind.OP != argument.getKind()) {
        continue;
      }

      final Primitive primitive = (Primitive) argument.getValue();
      if (Primitive.Kind.MODE == primitive.getKind()) {
        if (argument.getMode().isOut()) {
          modes.add(new AddressingModeWrapper(primitive));
        }
      } else {
        saveOutAddressingModes(primitive, modes);
      }
    }
  }

  public static void checkOp(final Primitive op) {
    checkNotNull(op);
    checkTrue(
        Primitive.Kind.OP == op.getKind(),
        String.format("%s is not an operation.", op.getName())
        );
  }

  public static void checkMode(final Primitive mode) {
    checkNotNull(mode);
    checkTrue(
        Primitive.Kind.MODE == mode.getKind(),
        String.format("%s is not an addressing mode.", mode.getName())
        );
  }

  public static void checkRootOp(final Primitive op) {
    checkOp(op);
    checkTrue(op.isRoot(), op.getName() + " is not a root operation!");
  }

  public static void checkArgKind(final Argument arg, final Argument.Kind expected) {
    checkNotNull(arg);
    checkTrue(
        arg.getKind() == expected,
        String.format("Argument %s has kind %s while %s is expected.",
            arg.getName(), arg.getKind(), expected)
        );
  }
}
