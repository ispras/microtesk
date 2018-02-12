/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.model.metadata.MetaShortcut;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PrimitiveBuilderOperation implements PrimitiveBuilder {
  private final String name;
  private String contextName;
  private Variate<Situation> situation;

  private final MetaModel metaModel;
  private final AbstractCallBuilder callBuilder;

  private final List<Argument> argumentList;
  private final Map<String, Argument> argumentMap;

  private static final String ERR_WRONG_USE =
      "Illegal use: Arguments can be added using either "
          + "addArgument or setArgument methods, but not both.";

  PrimitiveBuilderOperation(
      final String name,
      final MetaModel metaModel,
      final AbstractCallBuilder callBuilder) {

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(callBuilder);

    this.metaModel = metaModel;
    this.callBuilder = callBuilder;

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
          metaModel, callBuilder, metaShortcut.getOperation(), contextName);
    } else {
      // If there is no shortcut for the given context, the operation is used as it is.
      builder = new PrimitiveBuilderCommon(
          metaModel, callBuilder, metaData, null);
    }

    builder.setSituation(situation);

    if (!argumentList.isEmpty()) {
      for (final Argument argument : argumentList) {
        argument.addToBuilder(builder);
      }
    } else if (!argumentMap.isEmpty()) {
      for (final Argument argument : argumentMap.values()) {
        argument.addToBuilder(builder);
      }
    }

    return builder.build();
  }

  public void setContext(String contextName) {
    this.contextName = contextName;
  }

  public void setSituation(Situation situation) {
    this.situation = new VariateSingleValue<>(situation);
  }

  public void setSituation(Variate<Situation> situation) {
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
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentStr(value));
  }

  public void addArgument(RandomValue value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentRandVal(value));
  }

  public void addArgument(Primitive value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrim(value));
  }

  @Override
  public void addArgument(PrimitiveBuilder value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrimB(value));
  }

  @Override
  public void addArgument(UnknownImmediateValue value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentUnkVal(value));
  }

  @Override
  public void addArgument(LazyValue value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentLazyVal(value));
  }

  @Override
  public void addArgument(LabelValue value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentLabel(value));
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Hash-based syntax

  public void setArgument(String name, BigInteger value) {
    InvariantChecks.checkNotNull(name);
    registerArgument(new ArgumentInt(name, value));
  }

  public void setArgument(String name, String value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentStr(name, value));
  }

  public void setArgument(String name, RandomValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentRandVal(name, value));
  }

  public void setArgument(String name, Primitive value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrim(name, value));
  }

  @Override
  public void setArgument(String name, PrimitiveBuilder value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrimB(name, value));
  }

  @Override
  public void setArgument(String name, UnknownImmediateValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentUnkVal(name, value));
  }

  @Override
  public void setArgument(String name, LazyValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentLazyVal(name, value));
  }

  @Override
  public void setArgument(String name, LabelValue value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
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
      } else {
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
      } else {
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
      } else {
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
      } else {
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
      } else {
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
      } else {
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
      } else {
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
      } else {
        builder.addArgument(getValue());
      }
    }
  }
}
