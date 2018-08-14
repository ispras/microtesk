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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.utils.Mask;
import ru.ispras.microtesk.utils.SharedObject;
import ru.ispras.microtesk.utils.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Preparator {
  private final Where where;
  private final boolean isComparator;

  private final LazyPrimitive targetHolder;
  private final LazyData dataHolder;

  private final String name;
  private final Mask mask;
  private final List<Argument> arguments;

  private final Variate<List<AbstractCall>> calls;
  private final Map<String, Variant> variants;

  private final LabelUniqualizer.SeriesId labelSeriesId;

  protected Preparator(
      final Where where,
      final boolean isComparator,
      final LazyPrimitive targetHolder,
      final LazyData dataHolder,
      final String name,
      final Mask mask,
      final List<Argument> arguments,
      final List<AbstractCall> calls,
      final List<Variant> variants) {
    InvariantChecks.checkNotNull(where);
    InvariantChecks.checkNotNull(targetHolder);
    InvariantChecks.checkNotNull(dataHolder);
    InvariantChecks.checkNotNull(arguments);
    InvariantChecks.checkNotNull(calls);
    InvariantChecks.checkNotNull(variants);
    InvariantChecks.checkTrue(calls.isEmpty() || variants.isEmpty());

    this.where = where;
    this.isComparator = isComparator;

    this.targetHolder = targetHolder;
    this.dataHolder = dataHolder;

    this.name = name;
    this.mask = mask;
    this.arguments = arguments;

    if (!variants.isEmpty()) {
      final VariateBuilder<List<AbstractCall>> callBuilder = new VariateBuilder<>();
      final Map<String, Variant> variantMap = new HashMap<>();

      for (final Variant variant : variants) {
        if (variant.isBiased()) {
          callBuilder.addValue(variant.getCalls(), variant.getBias());
        } else {
          callBuilder.addValue(variant.getCalls());
        }

        if (null != variant.getName()) {
          variantMap.put(variant.getName(), variant);
        }
      }
      this.calls = callBuilder.build();
      this.variants = Collections.unmodifiableMap(variantMap);
    } else {
      this.calls = new VariateSingleValue<>(calls);
      this.variants = Collections.emptyMap();
    }

    this.labelSeriesId = LabelUniqualizer.get().newSeries();
  }

  public Where getWhere() {
    return where;
  }

  public boolean isComparator() {
    return isComparator;
  }

  public String getTargetName() {
    return targetHolder.getName();
  }

  public boolean isDefault() {
    return null == mask && arguments.isEmpty();
  }

  public boolean isMatch(
      final Primitive target,
      final BitVector data,
      final String preparatorName) {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(data);

    if (!target.getName().equals(getTargetName())) {
      return false;
    }

    if (null == name ? name != preparatorName : !name.equals(preparatorName)) {
      return false;
    }

    if (null != mask && !mask.isMatch(data)) {
      return false;
    }

    for (final Argument argument : arguments) {
      final BigInteger value =
          target.getArguments().get(argument.getName()).getImmediateValue();

      if (!argument.isMatch(value)) {
        return false;
      }
    }

    return true;
  }

  public List<AbstractCall> makeInitializer(
      final PreparatorStore preparators,
      final Primitive target,
      final BitVector data,
      final String preferedVariantName) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(data);

    targetHolder.setSource(target);
    dataHolder.setValue(data);

    final List<AbstractCall> chosenCalls = chooseCalls(preferedVariantName);
    return expandPreparators(labelSeriesId, preparators, chosenCalls);
  }

  public static List<AbstractCall> expandPreparators(
      final LabelUniqualizer.SeriesId labelSeriesId,
      final PreparatorStore preparators,
      final List<AbstractCall> calls) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(calls);

    if (null != labelSeriesId) {
      LabelUniqualizer.get().pushLabelScope(labelSeriesId);
    }

    final List<AbstractCall> expandedCalls = new ArrayList<>();
    for (final AbstractCall call : calls) {
      if (call.isPreparatorCall()) {
        final PreparatorReference reference = call.getPreparatorReference();

        final Primitive target = reference.getTarget();
        final BitVector data = reference.getValue();
        final String preparatorName = reference.getPreparatorName();
        final String variantName = reference.getVariantName();

        final Preparator preparator =
            preparators.getPreparator(target, data, preparatorName);

        if (null == preparator) {
          throw new GenerationAbortedException(String.format(
              "No suitable preparator is found for %s.", target.getSignature()));
        }

        final List<AbstractCall> expanded =
            preparator.makeInitializer(preparators, target, data, variantName);

        expandedCalls.addAll(expanded);
      } else {
        if (null != labelSeriesId) {
          final AbstractCall callCopy = new AbstractCall(call);
          LabelUniqualizer.get().makeLabelsUnique(callCopy);
          expandedCalls.add(callCopy);
        } else {
          // Copies are needed only when there is a specific preparator series to make its calls
          // unique. Otherwise, it will be redundant copying breaking links between shared objects.
          expandedCalls.add(call);
        }
      }
    }

    if (null != labelSeriesId) {
      LabelUniqualizer.get().popLabelScope();
    }

    SharedObject.freeSharedCopies();
    return expandedCalls;
  }

  protected List<AbstractCall> chooseCalls(final String preferedVariantName) {
    if (null != preferedVariantName) {
      final Variant variant = variants.get(preferedVariantName);
      if (null != variant) {
        return variant.getCalls();
      } else {
        Logger.warning(
            "The %s variant is not defined for the current preparator. "
                + "A random variant will be chosen.",
            preferedVariantName
        );
      }
    }
    return calls.value();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Preparator other = (Preparator) obj;

    if (this.isComparator != other.isComparator) {
      return false;
    }

    if (!this.getTargetName().equals(other.getTargetName())) {
      return false;
    }

    if (!(this.name == null ? other.name == null : this.name.equals(other.name))) {
      return false;
    }

    if (!(this.mask == null ? other.mask == null : this.mask.equals(other.mask))) {
      return false;
    }

    return this.arguments.equals(other.arguments);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append(isComparator ? "comparator" : "preparator");
    sb.append(String.format("(:target => '%s'", getTargetName()));

    if (null != name) {
      sb.append(String.format(", :name => '%s'", name));
    }

    if (null != mask) {
      sb.append(String.format(", :mask => %s", mask));
    }

    if (!arguments.isEmpty()) {
      sb.append(", :arguments => {");
      sb.append(StringUtils.toString(arguments, ", "));
      sb.append('}');
    }

    sb.append(')');
    return sb.toString();
  }

  protected static final class Variant {
    private final String name;
    private final boolean biased;
    private final int bias;
    private final List<AbstractCall> calls;

    protected Variant(final String name, final int bias) {
      this.name = name;
      this.biased = true;
      this.bias = bias;
      this.calls = new ArrayList<>();
    }

    protected Variant(final String name) {
      this.name = name;
      this.biased = false;
      this.bias = 0;
      this.calls = new ArrayList<>();
    }

    public String getName() {
      return name;
    }

    public boolean isBiased() {
      return biased;
    }

    public int getBias() {
      return bias;
    }

    public List<AbstractCall> getCalls() {
      return Collections.unmodifiableList(calls);
    }

    public void addCall(final AbstractCall call) {
      InvariantChecks.checkNotNull(call);
      calls.add(call);
    }
  }

  protected abstract static class Argument {
    public static Argument newValue(
        final String name,
        final BigInteger value) {
      InvariantChecks.checkNotNull(value);
      return  new ArgumentList(name, Collections.singletonList(value));
    }

    public static Argument newCollection(
        final String name,
        final Collection<BigInteger> values) {
      return new ArgumentList(name, new ArrayList<>(values));
    }

    public static Argument newRange(
        final String name,
        final BigInteger from,
        final BigInteger to) {
      return new ArgumentRange(name, from, to);
    }

    private final String name;

    protected Argument(final String name) {
      InvariantChecks.checkNotNull(name);
      this.name = name;
    }

    public final String getName() {
      return name;
    }

    public abstract boolean isMatch(BigInteger value);
  }

  private static final class ArgumentList extends Argument {
    private final Collection<BigInteger> values;

    protected ArgumentList(
        final String name,
        final List<BigInteger> values) {
      super(name);
      InvariantChecks.checkNotEmpty(values);
      this.values = values;
    }

    @Override
    public boolean isMatch(final BigInteger value) {
      for (final BigInteger currentValue : values) {
        if (currentValue.equals(value)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final ArgumentList other = (ArgumentList) obj;
      if (!this.getName().equals(other.getName())) {
        return false;
      }

      return this.values.equals(other.values);
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(String.format(":%s => ", getName()));

      final boolean isSingle = values.size() == 1;
      if (!isSingle) {
        sb.append('[');
      }

      sb.append(StringUtils.toString(values, ", "));

      if (!isSingle) {
        sb.append(']');
      }

      return sb.toString();
    }
  }

  private static final class ArgumentRange extends Argument {
    private final BigInteger from;
    private final BigInteger to;

    protected ArgumentRange(
        final String name,
        final BigInteger from,
        final BigInteger to) {
      super(name);

      InvariantChecks.checkNotNull(from);
      InvariantChecks.checkNotNull(to);

      this.from = from;
      this.to = to;
    }

    @Override
    public boolean isMatch(final BigInteger value) {
      InvariantChecks.checkNotNull(value);
      return value.compareTo(from) >= 0 && value.compareTo(to) <= 0;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final ArgumentRange other = (ArgumentRange) obj;
      if (!this.getName().equals(other.getName())) {
        return false;
      }

      return this.from.equals(other.from) && this.to.equals(other.to);
    }

    @Override
    public String toString() {
      return String.format(":%s => %d..%d", getName(), from, to);
    }
  }
}
