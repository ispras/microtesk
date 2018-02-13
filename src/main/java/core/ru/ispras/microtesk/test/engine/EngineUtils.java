/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.InstructionCall;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.IsaPrimitiveBuilder;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.LabelReference;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Preparator;
import ru.ispras.microtesk.test.template.PreparatorStore;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.Stream;
import ru.ispras.microtesk.test.template.StreamStore;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestBaseUtils;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link EngineUtils} implements functions shared among test data generators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class EngineUtils {
  private EngineUtils() {}

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final InitializerMaker.Stage stage,
      final AbstractCall abstractCall,
      final AbstractSequence abstractSequence,
      final Primitive primitive,
      final Situation situation) throws ConfigurationException {
    return makeInitializer(
        engineContext,
        processingCount,
        stage,
        abstractCall,
        abstractSequence,
        primitive,
        situation,
        null
        );
  }

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final int processingCount,
      final InitializerMaker.Stage stage,
      final AbstractCall abstractCall,
      final AbstractSequence abstractSequence,
      final Primitive primitive,
      final Situation situation,
      final IsaPrimitive concretePrimitive) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);
    // Parameter {@code situation} can be null.
    // Parameter {@code concretePrimitive} can be null.

    final TestBaseQueryCreator queryCreator = new TestBaseQueryCreator(
        engineContext,
        processingCount,
        abstractSequence,
        situation,
        primitive
        );

    final TestData testData = getTestData(engineContext, primitive, situation, queryCreator);
    Logger.debug(testData.toString());

    if (null != situation) {
      situation.setTestData(testData);
    }

    if (testData.isEmpty()) {
      return Collections.emptyList();
    }

    // Immediates are assigned only once.
    if (0 == processingCount) {
      setUnknownImmValues(
          testData,
          queryCreator.getUnknownImmValues(),
          null != concretePrimitive ? concretePrimitive.getArguments() : null
      );
    }

    final InitializerMaker initializerMaker = 
        EngineConfig.get().getInitializerMaker(testData.getId());

    InvariantChecks.checkNotNull(
        initializerMaker, "Initializer maker is undefined for " + testData.getId());

    return initializerMaker.makeInitializer(
        engineContext,
        processingCount,
        stage,
        abstractCall,
        primitive,
        situation,
        testData,
        queryCreator.getTargetModes()
        );
  }

  public static List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final Primitive mode,
      final BitVector value) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(value);

    final PreparatorStore preparators = engineContext.getPreparators();
    final Preparator preparator = preparators.getPreparator(mode, value, null);

    if (null != preparator) {
      return preparator.makeInitializer(preparators, mode, value, null);
    }

    throw new GenerationAbortedException(String.format(
        "No suitable preparator is found for %s.", mode.getSignature()));
  }

  public static TestData getTestData(
      final EngineContext engineContext,
      final Primitive primitive,
      final Situation situation,
      final TestBaseQueryCreator queryCreator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkNotNull(queryCreator);

    Logger.debug("Processing %s for %s...", situation, primitive.getSignature());

    final TestBaseQuery query = queryCreator.getQuery();
    Logger.debug("Query to TestBase: " + query);

    final Map<String, Argument> unknownImmediateValues = queryCreator.getUnknownImmValues();
    Logger.debug("Unknown immediate values: " + unknownImmediateValues.keySet());

    final Map<String, Primitive> modes = queryCreator.getTargetModes();
    Logger.debug("Modes used as input arguments: " + modes);

    final boolean isDefaultTestData =
        engineContext.getOptions().getValueAsBoolean(Option.DEFAULT_TEST_DATA);

    if (situation == null) {
      return isDefaultTestData ? TestBaseUtils.newRandomTestData(query) : TestData.EMPTY;
    }

    final TestBase testBase = TestBase.get();
    final TestBaseQueryResult queryResult = testBase.executeQuery(query);

    if (TestBaseQueryResult.Status.OK != queryResult.getStatus()) {
      Logger.warning("Query processing has failed: %s", queryResult);
      Logger.warning("Default test data will be used.");
      return TestBaseUtils.newRandomTestData(query);
    }

    final Iterator<TestData> dataIterator = queryResult.getDataIterator();
    dataIterator.init();

    if (!dataIterator.hasValue()) {
      return TestData.EMPTY;
    }

    return dataIterator.value();
  }

  public static void setUnknownImmValue(
      final NodeValue value,
      final Argument argument,
      final Immediate argumentToPatch) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkNotNull(argument);

    final BigInteger dataValue;
    if (value.isType(DataTypeId.LOGIC_INTEGER)) {
      dataValue = value.getInteger();
    } else if (value.isType(DataTypeId.BIT_VECTOR)) {
      dataValue = value.getBitVector().bigIntegerValue();
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
      final TestData testData,
      final Map<String, Argument> unknownImmValues) {
    setUnknownImmValues(testData, unknownImmValues, null);
  }

  public static void setUnknownImmValues(
      final TestData testData,
      final Map<String, Argument> unknownImmValues,
      final Map<String, IsaPrimitive> argumentsToPatch) {
    InvariantChecks.checkNotNull(testData);
    InvariantChecks.checkNotNull(unknownImmValues);

    for (final Map.Entry<String, Argument> e : unknownImmValues.entrySet()) {
      final Argument argument = e.getValue();
      final NodeValue value = (NodeValue) testData.getBindings().get(e.getKey());

      if (null == value) {
        continue;
      }

      final Immediate argumentToPatch;
      if (null != argumentsToPatch) {
        argumentToPatch = (Immediate) argumentsToPatch.get(argument.getName());
        InvariantChecks.checkNotNull(argumentToPatch);
      } else {
        argumentToPatch = null;
      }

      setUnknownImmValue(value, argument, argumentToPatch);
    }
  }

  public static String getSituationName(final AbstractCall abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

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
  private static List<LocationAccessor> addressRefs = null;

  public static List<ConcreteCall> makeConcreteCalls(
      final EngineContext engineContext,
      final List<AbstractCall> abstractSequence) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

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
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);

    // A preparator call must be expanded when the preparator containing this call is instantiated.
    InvariantChecks.checkFalse(
        abstractCall.isPreparatorCall() , "Unexpanded preparator invocation.");

    if (!abstractCall.isExecutable()) {
      return new ConcreteCall(abstractCall);
    }

    try {
      labelRefs = new ArrayList<>();
      addressRefs = new ArrayList<>();

      final Primitive rootOp = abstractCall.getRootOperation();
      checkRootOp(rootOp);

      final IsaPrimitive op = makeConcretePrimitive(engineContext, rootOp);
      final InstructionCall executable = engineContext.getModel().newCall(op);

      return new ConcreteCall(
          abstractCall,
          executable,
          labelRefs.isEmpty() ? Collections.<LabelReference>emptyList() : labelRefs,
          addressRefs.isEmpty() ? Collections.<LocationAccessor>emptyList() : addressRefs
          );
    } finally {
      labelRefs = null;
      addressRefs = null;
    }
  }

  public static ConcreteCall makeSpecialConcreteCall(
      final EngineContext engineContext,
      final String instructionName) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(instructionName);

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

  public static boolean isStreamBased(final AbstractCall abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

    final Primitive rootOp = abstractCall.getRootOperation();
    checkRootOp(rootOp);

    return isStreamBased(rootOp);
  }

  public static boolean isStreamBased(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    final Situation situation = primitive.getSituation();
    if (null != situation && null != situation.getAttribute("stream")) {
      return true;
    }

    for (final Argument argument : primitive.getArguments().values()) {
      if (argument.getKind() == Argument.Kind.OP) {
        final Primitive argumentPrimitive = (Primitive) argument.getValue();
        InvariantChecks.checkNotNull(argumentPrimitive);

        if (isStreamBased(argumentPrimitive)) {
          return true;
        }
      }
    }

    return false;
  }

  private static BigInteger getImmediateValue(final Argument argument) {
    InvariantChecks.checkNotNull(argument);
    final Object value = argument.getValue();

    if (value instanceof BigInteger) {
      return (BigInteger) value;
    }

    if (value instanceof Value) {
      return ((Value) value).getValue();
    }

    throw new IllegalArgumentException(String.format(
        "Cannot get an immediate values from argument %s that has kind %s.",
        argument.getName(),
        argument.getKind()
        ));
  }

  public static IsaPrimitive makeConcretePrimitive(
      final EngineContext engineContext,
      final Primitive primitive) throws ConfigurationException {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(primitive);

    final String name = primitive.getName();
    final IsaPrimitiveBuilder builder;

    if (Primitive.Kind.MODE == primitive.getKind() ) {
      builder = engineContext.getModel().newMode(name);
    } else if (Primitive.Kind.OP == primitive.getKind()) {
      builder = engineContext.getModel().newOp(name, primitive.getContextName());
    } else {
      throw new IllegalArgumentException(String.format(
          String.format("%s is not an addressing mode or an operation.", primitive.getName())));
    }

    for (final Argument arg : primitive.getArguments().values()) {
      final String argName = arg.getName();
      switch (arg.getKind()) {
        case IMM:
        case IMM_RANDOM:
        case IMM_UNKNOWN:
          builder.setArgument(argName, getImmediateValue(arg));
          break;

        case IMM_LAZY: {
          final LocationAccessor locationAccessor =
              builder.setArgument(argName, getImmediateValue(arg));
          if (arg.getValue() == LazyValue.ADDRESS && addressRefs != null) {
            addressRefs.add(locationAccessor);
          }
          break;
        }

        case LABEL: {
          final LocationAccessor locationAccessor =
              builder.setArgument(argName, getImmediateValue(arg));
          final LabelReference labelReference =
              new LabelReference((LabelValue) arg.getValue(), locationAccessor);

          if (null != labelRefs) {
            labelRefs.add(labelReference);
          }

          if (primitive.isLabel()) {
            builder.setLabelReference(labelReference);
          }

          break;
        }

        case MODE:
        case OP:
          builder.setArgument(
              argName, makeConcretePrimitive(engineContext, (Primitive) arg.getValue()));
          break;

        default:
          throw new IllegalArgumentException(String.format(
              "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }

    return builder.build();
  }

  public static List<AbstractCall> makeStreamInit(
      final EngineContext engineContext, final String streamId) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getInit();
  }

  public static List<AbstractCall> makeStreamRead(
      final EngineContext engineContext, final String streamId) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getRead();
  }

  public static List<AbstractCall> makeStreamWrite(
      final EngineContext engineContext, final String streamId) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(streamId);

    final StreamStore streams = engineContext.getStreams();
    final Stream stream = streams.getStream(streamId);
    InvariantChecks.checkNotNull(stream);

    return stream.getWrite();
  }

  public static Set<AddressingModeWrapper> getOutAddressingModes(final List<AbstractCall> calls) {
    InvariantChecks.checkNotNull(calls);

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
      if (Argument.Kind.MODE != argument.getKind()
          && Argument.Kind.OP != argument.getKind()) {
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

  public static void checkRootOp(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    InvariantChecks.checkTrue(
        Primitive.Kind.OP == primitive.getKind(),
        String.format("%s is not an operation.", primitive.getName()));

    InvariantChecks.checkTrue(
        primitive.isRoot(),
        primitive.getName() + " is not a root operation!");
  }
}
