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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;
import ru.ispras.microtesk.test.template.Primitive.Kind;

public interface PrimitiveBuilder {
  Primitive build();

  void setContext(String contextName);
  void setSituation(Situation situation);

  void addArgument(BigInteger value);
  void addArgument(String value);
  void addArgument(RandomValue value);
  void addArgument(Primitive value);
  void addArgument(PrimitiveBuilder value);
  void addArgument(UnknownImmediateValue value);
  void addArgument(LazyValue value);
  void addArgument(LabelValue value);
  void setArgument(String name, BigInteger value);
  void setArgument(String name, String value);
  void setArgument(String name, RandomValue value);
  void setArgument(String name, Primitive value);
  void setArgument(String name, PrimitiveBuilder value);
  void setArgument(String name, UnknownImmediateValue value);
  void setArgument(String name, LazyValue value);
  void setArgument(String name, LabelValue value);
}

final class PrimitiveBuilderOperation implements PrimitiveBuilder {
  private final String name;
  private String contextName;
  private Situation situation;
  
  private final MetaModel metaModel;
  private final CallBuilder callBuilder;
  private final MemoryMap memoryMap;

  private final List<Argument> argumentList;
  private final Map<String, Argument> argumentMap;

  private static final String ERR_WRONG_USE =
      "Illegal use: Arguments can be added using either " +
      "addArgument or setArgument methods, but not both.";

  PrimitiveBuilderOperation(
      String name, MetaModel metaModel, CallBuilder callBuilder, MemoryMap memoryMap) {

    checkNotNull(name);
    checkNotNull(metaModel);
    checkNotNull(callBuilder);
    checkNotNull(memoryMap);

    this.metaModel = metaModel;
    this.callBuilder = callBuilder;
    this.memoryMap = memoryMap;

    this.name = name;
    this.contextName = null;
    this.situation = null;

    this.argumentList = new ArrayList<Argument>();
    this.argumentMap = new LinkedHashMap<String, Argument>();
  }

  public Primitive build() {
    final MetaOperation metaData = metaModel.getOperation(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such operation: " + name);
    }

    final MetaShortcut metaShortcut = metaData.getShortcut(contextName);

    final PrimitiveBuilder builder;
    if (null != metaShortcut) {
      builder = new PrimitiveBuilderCommon(
          metaModel, callBuilder, memoryMap, metaShortcut.getOperation(), contextName);
    } else {
      // If there is no shortcut for the given context, the operation is used as it is.
      builder = new PrimitiveBuilderCommon(
          metaModel, callBuilder, memoryMap, metaData, null);
    }

    builder.setSituation(situation);

    if (!argumentList.isEmpty()) {
      for (Argument argument : argumentList) {
        argument.addToBuilder(builder);
      }
    } else if (!argumentMap.isEmpty()) {
      for (Argument argument : argumentMap.values()) {
        argument.addToBuilder(builder);
      }
    }

    return builder.build();
  }

  public void setContext(String contextName) {
    this.contextName = contextName;
  }

  public void setSituation(Situation situation) {
    this.situation = situation;
  }

  private void registerArgument(Argument argument) {
    if (argument.hasName()) {
      if (!argumentList.isEmpty()) {
        throw new IllegalStateException(ERR_WRONG_USE);
      }
      argumentMap.put(argument.getName(), argument);
    } else {
      if (!argumentMap.isEmpty()) {
        throw new IllegalStateException(ERR_WRONG_USE);
      }
      argumentList.add(argument);
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Array-based syntax

  public void addArgument(BigInteger value) {
    registerArgument(new ArgumentInt(value));
  }

  public void addArgument(String value) {
    checkNotNull(value);
    registerArgument(new ArgumentStr(value));
  }

  public void addArgument(RandomValue value) {
    checkNotNull(value);
    registerArgument(new ArgumentRandVal(value));
  }

  public void addArgument(Primitive value) {
    checkNotNull(value);
    registerArgument(new ArgumentPrim(value));
  }

  @Override
  public void addArgument(PrimitiveBuilder value) {
    checkNotNull(value);
    registerArgument(new ArgumentPrimB(value));
  }

  @Override
  public void addArgument(UnknownImmediateValue value) {
    checkNotNull(value);
    registerArgument(new ArgumentUnkVal(value));
  }

  @Override
  public void addArgument(LazyValue value) {
    checkNotNull(value);
    registerArgument(new ArgumentLazyVal(value));
  }

  @Override
  public void addArgument(LabelValue value) {
    checkNotNull(value);
    registerArgument(new ArgumentLabel(value));
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Hash-based syntax

  public void setArgument(String name, BigInteger value) {
    checkNotNull(name);
    registerArgument(new ArgumentInt(name, value));
  }

  public void setArgument(String name, String value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentStr(name, value));
  }

  public void setArgument(String name, RandomValue value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentRandVal(name, value));
  }

  public void setArgument(String name, Primitive value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentPrim(name, value));
  }

  @Override
  public void setArgument(String name, PrimitiveBuilder value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentPrimB(name, value));
  }

  @Override
  public void setArgument(String name, UnknownImmediateValue value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentUnkVal(name, value));
  }

  @Override
  public void setArgument(String name, LazyValue value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentLazyVal(name, value));
  }
  
  @Override
  public void setArgument(String name, LabelValue value) {
    checkNotNull(name);
    checkNotNull(value);
    registerArgument(new ArgumentLabel(name, value));
  }

  private interface Argument {
    boolean hasName();
    String getName();
    void addToBuilder(PrimitiveBuilder builder);
  }

  private static abstract class AbstractArgument<T> implements Argument {
    private final String name;
    private final T value;

    public AbstractArgument(String name, T value) {
      this.name = name;
      this.value = value;
    }

    public AbstractArgument(T value) {
      this(null, value);
    }

    @Override
    public final String getName() {
      return name;
    }

    @Override
    public final boolean hasName() {
      return null != name;
    }

    public final T getValue() {
      return value;
    }
  }

  private static class ArgumentInt extends AbstractArgument<BigInteger> {
    public ArgumentInt(String name, BigInteger value) {
      super(name, value);
    }

    public ArgumentInt(BigInteger value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentStr extends AbstractArgument<String> {
    public ArgumentStr(String name, String value) {
      super(name, value);
    }

    public ArgumentStr(String value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentRandVal extends AbstractArgument<RandomValue> {
    public ArgumentRandVal(String name, RandomValue value) {
      super(name, value);
    }

    public ArgumentRandVal(RandomValue value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentPrim extends AbstractArgument<Primitive> {
    public ArgumentPrim(String name, Primitive value) {
      super(name, value);
    }

    public ArgumentPrim(Primitive value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentPrimB extends AbstractArgument<PrimitiveBuilder> {
    public ArgumentPrimB(String name, PrimitiveBuilder value) {
      super(name, value);
    }

    public ArgumentPrimB(PrimitiveBuilder value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentUnkVal extends AbstractArgument<UnknownImmediateValue> {
    public ArgumentUnkVal(String name, UnknownImmediateValue value) {
      super(name, value);
    }

    public ArgumentUnkVal(UnknownImmediateValue value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentLazyVal extends AbstractArgument<LazyValue> {
    public ArgumentLazyVal(String name, LazyValue value) {
      super(name, value);
    }

    public ArgumentLazyVal(LazyValue value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentLabel extends AbstractArgument<LabelValue> {
    public ArgumentLabel(String name, LabelValue value) {
      super(name, value);
    }

    public ArgumentLabel(LabelValue value) {
      super(value);
    }

    @Override
    public void addToBuilder(PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      }
      else {
        builder.addArgument(getValue());
      }
    }
  }
}

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
  private final CallBuilder callBuilder;
  private final MemoryMap memoryMap;

  private final Strategy strategy;
  private final Kind kind;
  private final Map<String, Argument> args;
  private String contextName;
  private Situation situation;

  private final LazyPrimitive lazyPrimitive; // Needed for label references.

  PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final CallBuilder callBuilder,
      final MemoryMap memoryMap,
      final MetaOperation metaData,
      final String contextName) {
    this(
        metaModel,
        callBuilder,
        memoryMap,
        new StrategyOperation(metaData, contextName),
        Kind.OP,
        contextName
        );
  }

  PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final CallBuilder callBuilder,
      final MemoryMap memoryMap,
      final MetaAddressingMode metaData) {
    this(
        metaModel,
        callBuilder,
        memoryMap,
        new StrategyAddressingMode(metaData),
        Kind.MODE,
        null
        );
  }

  private PrimitiveBuilderCommon(
      final MetaModel metaModel,
      final CallBuilder callBuilder,
      final MemoryMap memoryMap,
      final Strategy strategy,
      final Kind kind,
      final String contextName) {
    checkNotNull(metaModel);
    checkNotNull(callBuilder);
    checkNotNull(memoryMap);

    this.metaModel = metaModel;
    this.callBuilder = callBuilder;
    this.memoryMap = memoryMap;

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
        canThrowException()
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

  @Override
  public void setContext(final String contextName) {
    if (null != this.contextName) {
      throw new IllegalStateException("Context is already assigned.");
    }

    this.contextName = contextName;
  }

  public void setSituation(final Situation situation) {
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
    checkNotNull(name);

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, Argument.Kind.IMM, value, metaArg.getMode(), metaArg.getDataType());

    checkValidArgument(arg);
    putArgument(arg);
  }

  // For labels
  @Override
  public void setArgument(final String name, final String value) {
    checkNotNull(name);
    checkNotNull(value);

    final Label label = new Label(value, callBuilder.getBlockId()); 
    final LabelValue labelValue;

    // Address of labels in the data section are known when an abstract sequence is being created.
    // Addresses of labels in code are unknown will be resolved later (in concrete sequences,
    // immediately before simulation).

    if (memoryMap.isDefined(label.getName())) {
      final BigInteger address = memoryMap.resolve(label.getName());
      labelValue = LabelValue.newKnown(label, address);
    } else {
      labelValue = LabelValue.newUnknown(label);
    }

    setArgument(name, labelValue);
  }

  @Override
  public void setArgument(final String name, final RandomValue value) {
    checkNotNull(name);
    checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, Argument.Kind.IMM_RANDOM, value, metaArg.getMode(), metaArg.getDataType());

    checkValidArgument(arg);
    putArgument(arg);
  }

  @Override
  public void setArgument(final String name, final Primitive value) {
    checkNotNull(name);
    checkNotNull(value);

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
    checkNotNull(name);
    checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, Argument.Kind.IMM_UNKNOWN, value, metaArg.getMode(), metaArg.getDataType());

    checkValidArgument(arg);
    putArgument(arg);
  }
  
  @Override
  public void setArgument(final String name, final LazyValue value) {
    checkNotNull(name);
    checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, Argument.Kind.IMM_LAZY, value, metaArg.getMode(), metaArg.getDataType());

    checkValidArgument(arg);
    putArgument(arg);
  }

  // For lazy labels (used in data_stream blocks) 
  @Override
  public void setArgument(final String name, final LabelValue value) {
    checkNotNull(name);
    checkNotNull(value);

    final MetaArgument metaArg = getMetaArgument(name);

    final Argument arg = new Argument(
        name, Argument.Kind.LABEL, value, metaArg.getMode(), metaArg.getDataType());

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
      checkNotNull(metaData);

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
      checkNotNull(metaData);

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
}
