/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.IsaPrimitiveKind;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.model.metadata.MetaShortcut;
import ru.ispras.microtesk.test.template.Primitive.Kind;

final class PrimitiveBuilderCommon implements PrimitiveBuilder {
  private interface Strategy {
    String getName();
    String getTypeName();
    String getDescription();
    boolean isRoot();
    String getNextArgumentName();
    void checkValidArgument(Argument arg);
    void checkAllArgumentsAssigned(Set<String> argNames);
  }

  private final MetaModel metaModel; 
  private final AbstractCallBuilder callBuilder;

  private final Strategy strategy;
  private final Kind kind;
  private final Map<String, Argument> args;
  private String contextName;
  private Variate<Situation> situation;

  private final LazyPrimitive lazyPrimitive; // Needed for label references.

  PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final AbstractCallBuilder callBuilder,
      final MetaOperation metaData,
      final String contextName) {
    this(
        metaModel,
        callBuilder,
        new StrategyOperation(metaData, contextName),
        Kind.OP,
        contextName
        );
  }

  PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final AbstractCallBuilder callBuilder,
      final MetaAddressingMode metaData) {
    this(
        metaModel,
        callBuilder,
        new StrategyAddressingMode(metaData),
        Kind.MODE,
        null
        );
  }

  private PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final AbstractCallBuilder callBuilder,
      final Strategy strategy,
      final Kind kind,
      final String contextName) {
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(callBuilder);

    this.metaModel = metaModel;
    this.callBuilder = callBuilder;

    this.strategy = strategy;
    this.kind = kind;
    this.args = new LinkedHashMap<>();
    this.contextName = contextName;
    this.situation = null;

    this.lazyPrimitive = new LazyPrimitive(
      kind, strategy.getName(), strategy.getTypeName());
  }

  private void putArgument(final Argument arg) {
    args.put(arg.getName(), arg);
  }

  public Primitive build() {
    checkAllArgumentsSet(Collections.unmodifiableSet(args.keySet()));

    final Pair<Boolean, Boolean> branchInfo = getBranchInfo();
    final MemoryAccessStatus memAccessStatus = getMemoryAccessStatus();

    final Primitive primitive = new ConcretePrimitive(
        kind,
        getName(),
        strategy.getTypeName(),
        strategy.isRoot(),
        args,
        contextName,
        situation,
        branchInfo.first,
        branchInfo.second,
        canThrowException(),
        memAccessStatus.isLoad(),
        memAccessStatus.isStore(),
        memAccessStatus.getBlockSize()
        );

    lazyPrimitive.setSource(primitive);
    return primitive;
  }

  private boolean canThrowException() {
    if (kind == Kind.MODE) {
      // Modes have only immediate arguments and don't depend on their behavior.
      return metaModel.getAddressingMode(getName()).canThrowException();
    }

    if (getMetaOperation().canThrowException()) {
      return true;
    }

    for (final Argument arg : args.values()) {
      if (arg.getKind() != Argument.Kind.OP && arg.getKind() != Argument.Kind.MODE) {
        continue;
      }

      final Primitive primitive = (Primitive) arg.getValue();
      boolean exception = false;

      if (primitive instanceof ConcretePrimitive) {
        exception = primitive.canThrowException();
      } else if (primitive instanceof LazyPrimitive) {
        if (arg.getKind() == Argument.Kind.OP) {
          throw new IllegalStateException("LazyPrimitive cannot be an OP.");
        }

        exception = metaModel.getAddressingMode(primitive.getName()).canThrowException();
      } else {
        throw new IllegalArgumentException(
            "Unsupported primitive implementation: " + primitive.getClass().getName());
      }

      if (exception) {
        return true;
      }
    }

    return false;
  }

  private Pair<Boolean, Boolean> getBranchInfo() {
    if (kind == Kind.MODE) {
      // Modes do not perform control transfers.
      return new Pair<>(false, false);
    }

    final MetaOperation metaOperation = getMetaOperation(); 
    if (metaOperation.isBranch()) {
      return new Pair<>(metaOperation.isBranch(), metaOperation.isConditionalBranch());
    }

    for (final Argument arg : args.values()) {
      if (arg.getKind() != Argument.Kind.OP) {
        continue;
      }

      final Primitive primitive = (Primitive) arg.getValue();
      if (primitive instanceof ConcretePrimitive) {
        if (primitive.isBranch()) {
          return new Pair<>(primitive.isBranch(), primitive.isConditionalBranch());
        }
      } else {
        throw new IllegalArgumentException(
            "Unsupported primitive implementation: " + primitive.getClass().getName());
      }
    }

    return new Pair<>(false, false);
  }

  private MemoryAccessStatus getMemoryAccessStatus() {
    if (kind == Kind.MODE) {
      return MemoryAccessStatus.NO;
    }

    MemoryAccessStatus result = MemoryAccessStatus.NO; 

    final MetaOperation metaOperation = getMetaOperation();
    result = result.merge(new MemoryAccessStatus(
        metaOperation.isLoad(),
        metaOperation.isStore(),
        metaOperation.getBlockSize())
        );

    for (final Argument arg : args.values()) {
      if (arg.getKind() != Argument.Kind.OP) {
        continue;
      }

      final Primitive primitive = (Primitive) arg.getValue();
      result = result.merge(new MemoryAccessStatus(
          primitive.isLoad(),
          primitive.isStore(),
          primitive.getBlockSize())
          );
    }

    return result;
  }

  @Override
  public void setContext(final String contextName) {
    if (null != this.contextName) {
      throw new IllegalStateException("Context is already assigned.");
    }

    this.contextName = contextName;
  }

  public void setSituation(final Situation situation) {
    this.situation = new VariateSingleValue<>(situation);
  }

  public void setSituation(final Variate<Situation> situation) {
    this.situation = situation;
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Array-based syntax

  public void addArgument(final BigInteger value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  // For labels
  public void addArgument(final String value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  public void addArgument(final RandomValue value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  public void addArgument(final Primitive value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  @Override
  public void addArgument(final PrimitiveBuilder value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  @Override
  public void addArgument(final UnknownImmediateValue value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  @Override
  public void addArgument(final LazyValue value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  @Override
  public void addArgument(final LabelValue value) {
    final String name = getNextArgumentName();
    setArgument(name, value);
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Hash-based syntax

  @Override
  public void setArgument(final String name, final BigInteger value) {
    InvariantChecks.checkNotNull(name);

    final MetaArgument metaArg = getMetaArgument(name);
    final Argument arg;

    if (metaArg.getKind() == IsaPrimitiveKind.MODE) {
      final PrimitiveBuilder builder = newModeBuilder(metaArg);
      builder.addArgument(value);
      arg = newModeArgument(name, builder.build(), metaArg);
    } else {
      arg = new Argument(
          name, Argument.Kind.IMM, value, metaArg.getMode(), metaArg.getDataType());
    }

    checkValidArgument(arg);
    putArgument(arg);
  }

  // For labels
  @Override
  public void setArgument(final String name, final String value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    final Label label = new Label(value, callBuilder.getBlockId()); 
    final LabelValue labelValue = LabelValue.newUnknown(label);

    setArgument(name, labelValue);
  }

  @Override
  public void setArgument(final String name, final RandomValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);
    final Argument arg;

    if (metaArg.getKind() == IsaPrimitiveKind.MODE) {
      final PrimitiveBuilder builder = newModeBuilder(metaArg);
      builder.addArgument(value);
      arg = newModeArgument(name, builder.build(), metaArg);
    } else {
      arg = new Argument(
          name, Argument.Kind.IMM_RANDOM, value, metaArg.getMode(), metaArg.getDataType());
    }

    checkValidArgument(arg);
    putArgument(arg);
  }

  @Override
  public void setArgument(final String name, final Primitive value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    if ((value.getKind() != Primitive.Kind.MODE) && (value.getKind() != Primitive.Kind.OP)) {
      throw new IllegalArgumentException("Unsupported primitive kind: " + value.getKind());
    }

    final Argument.Kind kind =
      value.getKind() == Primitive.Kind.MODE ? Argument.Kind.MODE : Argument.Kind.OP;

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, kind, value, metaArg.getMode(), metaArg.getDataType());

    checkValidArgument(arg);
    putArgument(arg);
  }

  @Override
  public void setArgument(final String name, final PrimitiveBuilder value) {
    value.setContext(getName());
    setArgument(name, value.build());
  }

  @Override
  public void setArgument(final String name, final UnknownImmediateValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);
    value.setType(metaArg.getDataType());

    final Argument arg;
    if (metaArg.getKind() == IsaPrimitiveKind.MODE) {
      final PrimitiveBuilder builder = newModeBuilder(metaArg);
      builder.addArgument(value);
      arg = newModeArgument(name, builder.build(), metaArg);
    } else {
      arg = new Argument(
          name, Argument.Kind.IMM_UNKNOWN, value, metaArg.getMode(), metaArg.getDataType());
    }

    checkValidArgument(arg);
    putArgument(arg);
  }

  @Override
  public void setArgument(final String name, final LazyValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);
    final Argument arg;

    if (metaArg.getKind() == IsaPrimitiveKind.MODE) {
      final PrimitiveBuilder builder = newModeBuilder(metaArg);
      builder.addArgument(value);
      arg = newModeArgument(name, builder.build(), metaArg);
    } else {
      arg = new Argument(
          name, Argument.Kind.IMM_LAZY, value, metaArg.getMode(), metaArg.getDataType());
    }

    checkValidArgument(arg);
    putArgument(arg);
  }

  // For lazy labels (used in data_stream blocks) 
  @Override
  public void setArgument(final String name, final LabelValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);
    final Argument arg;

    if (metaArg.getKind() == IsaPrimitiveKind.MODE) {
      final PrimitiveBuilder builder = newModeBuilder(metaArg);
      builder.addArgument(value);
      arg = newModeArgument(name, builder.build(), metaArg);
    } else {
      arg = new Argument(
          name, Argument.Kind.LABEL, value, metaArg.getMode(), metaArg.getDataType());
    }

    checkValidArgument(arg);
    putArgument(arg);

    callBuilder.addLabelReference(value);
  }

  private String getName() {
    return strategy.getName();
  }

  private String getNextArgumentName() {
    return strategy.getNextArgumentName();
  }

  private void checkValidArgument(final Argument arg) {
    strategy.checkValidArgument(arg);
  }

  private void checkAllArgumentsSet(final Set<String> argNames) {
    strategy.checkAllArgumentsAssigned(argNames);
  }

  private MetaArgument getMetaArgument(final String name) {
    if (kind == Kind.MODE) {
      return metaModel.getAddressingMode(getName()).getArgument(name);
    }

    if (kind == Kind.OP) {
      final MetaOperation metaOp = getMetaOperation();
      return metaOp.getArgument(name);
    }

    throw new IllegalStateException("Illegal kind: " + kind);
  }

  private MetaOperation getMetaOperation() {
    final MetaOperation metaOp = metaModel.getOperation(getName());
    final MetaShortcut metaShortcut = metaOp.getShortcut(contextName);
    return (null != metaShortcut) ? metaShortcut.getOperation() : metaOp;
  }

  private static String getModeName(final MetaArgument metaArgument) {
    InvariantChecks.checkTrue(metaArgument.getKind() == IsaPrimitiveKind.MODE,
                              "The argument must be an addressing mode!");
    InvariantChecks.checkTrue(metaArgument.getTypeNames().size() == 1,
                              "The argument must not be an OR-rule!");
    return metaArgument.getTypeNames().iterator().next();
  }

  private PrimitiveBuilder newModeBuilder(final String modeName) {
    final MetaAddressingMode mode = metaModel.getAddressingMode(modeName);
    return new PrimitiveBuilderCommon(metaModel, callBuilder, mode);
  }

  private PrimitiveBuilder newModeBuilder(final MetaArgument metaArgument) {
    return newModeBuilder(getModeName(metaArgument));
  }

  private static Argument newModeArgument(
      final String argumentName,
      final Primitive argumentPrimitive,
      final MetaArgument metaArgument) {
    return new Argument(
        argumentName,
        Argument.Kind.MODE,
        argumentPrimitive,
        metaArgument.getMode(),
        metaArgument.getDataType()
        );
  }

  private static final String ERR_UNASSIGNED_ARGUMENT = 
      "The %s argument of %s is not assigned.";

  private static final String ERR_NO_MORE_ARGUMENTS =
      "Too many arguments: %s has only %d arguments.";

  private static final String ERR_UNDEFINED_ARGUMENT = 
      "The %s argument is not defined for %s.";

  private static final String ERR_TYPE_NOT_ACCEPTED =
      "The %s type is not accepted for the %s argument of %s.";

  private static final class StrategyOperation implements Strategy {
    private final MetaOperation metaData;
    private final String contextName;

    private int argumentCount;
    private final Iterator<MetaArgument> argumentIterator;

    StrategyOperation(final MetaOperation metaData, final String contextName) {
      InvariantChecks.checkNotNull(metaData);

      this.metaData = metaData;
      this.contextName = contextName;

      this.argumentCount = 0;
      this.argumentIterator = metaData.getArguments().iterator();
    }

    @Override
    public String getName() {
      return metaData.getName();
    }

    @Override
    public String getTypeName() {
      return metaData.getTypeName();
    }

    @Override
    public String getDescription() {
      final String basicDescription = String.format("the %s operation", getName());

      if (null == contextName) {
        return basicDescription;
      }

      return String.format("%s (shortcut for context %s)", basicDescription, contextName);
    }

    @Override
    public boolean isRoot() {
      return metaData.isRoot();
    }

    @Override
    public String getNextArgumentName() {
      if (!argumentIterator.hasNext()) {
        throw new IllegalStateException(String.format(
            ERR_NO_MORE_ARGUMENTS, getDescription(), argumentCount));
      }

      final MetaArgument argument = argumentIterator.next();
      argumentCount++;

      return argument.getName();
    }

    @Override
    public void checkValidArgument(final Argument arg) {
      final MetaArgument metaArgument = metaData.getArgument(arg.getName());

      if (null == metaArgument) {
        throw new IllegalStateException(String.format(
            ERR_UNDEFINED_ARGUMENT, arg.getName(), getDescription()));
      }

      final String typeName = arg.getTypeName();
      if (!metaArgument.isTypeAccepted(typeName)) {
        throw new IllegalStateException(String.format(
            ERR_TYPE_NOT_ACCEPTED, typeName, arg.getName(), getDescription()));
      }
    }

    @Override
    public void checkAllArgumentsAssigned(final Set<String> argNames) {
      for (final MetaArgument arg : metaData.getArguments()) {
        if (!argNames.contains(arg.getName())) {
          throw new IllegalStateException(String.format(
              ERR_UNASSIGNED_ARGUMENT, arg.getName(), getDescription()));
        }
      }
    }
  }

  private static final class StrategyAddressingMode implements Strategy {
    private static final String ERR_WRONG_ARGUMENT_KIND =
        "The %s argument of %s is %s, but it must be an immediate value.";

    private final MetaAddressingMode metaData;

    private int argumentCount;
    private final Iterator<String> argumentNameIterator;

    StrategyAddressingMode(final MetaAddressingMode metaData) {
      InvariantChecks.checkNotNull(metaData);

      this.metaData = metaData;
      this.argumentCount = 0;
      this.argumentNameIterator = metaData.getArgumentNames().iterator();
    }

    @Override
    public String getName() {
      return metaData.getName();
    }

    @Override
    public String getTypeName() {
      return getName();
    }

    @Override
    public String getDescription() {
      return String.format("the %s addressing mode", getName());
    }

    @Override
    public boolean isRoot() {
      // Always false because addressing modes always have parents.
      return false;
    }

    @Override
    public String getNextArgumentName() {
      if (!argumentNameIterator.hasNext()) {
        throw new IllegalStateException(String.format(
            ERR_NO_MORE_ARGUMENTS, getDescription(), argumentCount));
      }

      final String argumentName = argumentNameIterator.next();
      argumentCount++;

      return argumentName;
    }

    @Override
    public void checkValidArgument(final Argument arg) {
      if (!metaData.isArgumentDefined(arg.getName())) {
        throw new IllegalStateException(String.format(
            ERR_UNDEFINED_ARGUMENT, arg.getName(), getDescription()));
      }

      if (!arg.isImmediate()) {
        throw new IllegalStateException(String.format(
            ERR_WRONG_ARGUMENT_KIND, arg.getName(), getDescription(), arg.getKind()));
      }
    }

    @Override
    public void checkAllArgumentsAssigned(final Set<String> argNames) {
      for (final String argName : metaData.getArgumentNames()) {
        if (!argNames.contains(argName)) {
          throw new IllegalStateException(String.format(
              ERR_UNASSIGNED_ARGUMENT, argName, getDescription()));
        }
      }
    }
  }

  final static class MemoryAccessStatus {
    public static final MemoryAccessStatus NO =
        new MemoryAccessStatus(false, false, 0);

    private boolean load;
    private boolean store;
    private int blockSize;

    public MemoryAccessStatus(
        final boolean load,
        final boolean store,
        final int blockSize) {
      this.load = load;
      this.store = store;
      this.blockSize = blockSize;
    }

    public boolean isLoad() { return load; }
    public boolean isStore() { return store; }
    public int getBlockSize() { return blockSize; }

    public MemoryAccessStatus merge(final MemoryAccessStatus other) {
      return new MemoryAccessStatus(
          this.load  || other.load,
          this.store || other.store,
          Math.max(this.blockSize, other.blockSize)
          );
    }

    @Override
    public String toString() {
      return String.format(
          "MemoryAccessStatus [isLoad=%s, isStore=%s, blockSize=%s]",
          load,
          store,
          blockSize
          );
    }
  }
}
