/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateBuilder;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.GenerationAbortedException;

public final class Preparator {
  private final boolean isComparator;

  private final LazyPrimitive targetHolder;
  private final LazyData dataHolder;

  private final Mask mask;
  private final List<Argument> arguments;

  private final Variate<List<Call>> calls;
  private final Map<String, Variant> variants;

  protected Preparator(
      final boolean isComparator,
      final LazyPrimitive targetHolder,
      final LazyData dataHolder,
      final Mask mask,
      final List<Argument> arguments,
      final List<Call> calls,
      final List<Variant> variants) {
    InvariantChecks.checkNotNull(targetHolder);
    InvariantChecks.checkNotNull(dataHolder);
    InvariantChecks.checkNotNull(arguments);
    InvariantChecks.checkNotNull(calls);
    InvariantChecks.checkNotNull(variants);
    InvariantChecks.checkTrue(calls.isEmpty() || variants.isEmpty());

    this.isComparator = isComparator;

    this.targetHolder = targetHolder;
    this.dataHolder = dataHolder;

    this.mask = mask;
    this.arguments = arguments;

    if (!variants.isEmpty()) {
      final VariateBuilder<List<Call>> callBuilder = new VariateBuilder<>();
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

  public boolean isMatch(final Primitive target, final BitVector data) {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(data);

    if (!target.getName().equals(getTargetName())) {
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

  public List<Call> makeInitializer(
      final PreparatorStore preparators,
      final Primitive target,
      final BitVector data,
      final String preferedVariantName) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(data);

    targetHolder.setSource(target);
    dataHolder.setValue(data);

    final List<Call> chosenCalls = chooseCalls(preferedVariantName);
    return expandCalls(preparators, chosenCalls);
  }

  public static List<Call> expandCalls(
      final PreparatorStore preparators,
      final List<Call> calls) {
    InvariantChecks.checkNotNull(preparators);
    InvariantChecks.checkNotNull(calls);

    final List<Call> expandedCalls = new ArrayList<>();
    for (final Call call : calls) {
      if (call.isPreparatorCall()) {
        final PreparatorReference reference = call.getPreparatorReference();

        final BitVector data = reference.getValue().getData().getValue();
        final Primitive target = reference.getTarget();
        final String variantName = reference.getVariantName();

        final Preparator preparator = preparators.getPreparator(target, data);
        if (null == preparator) {
          throw new GenerationAbortedException(String.format(
              "No suitable preparator is found for %s.", target.getSignature()));
        }

        final List<Call> expanded =
            preparator.makeInitializer(preparators, target, data, variantName);

        expandedCalls.addAll(expanded);
      } else {
        expandedCalls.add(new Call(call));
      }
    }

    return expandedCalls;
  }

  protected List<Call> chooseCalls(final String preferedVariantName) {
    if (null != preferedVariantName) {
      final Variant variant = variants.get(preferedVariantName);
      if (null != variant) {
        return variant.getCalls();
      } else {
        Logger.warning(
            "The %s variant is not defined for the current preparator. " +
            "A random variant will be chosen.",
            preferedVariantName
            );
      }
    }
    return calls.value();
  }

  protected static final class Variant {
    private final String name;
    private final boolean biased;
    private final int bias;
    private final List<Call> calls;

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

    public List<Call> getCalls() {
      return Collections.unmodifiableList(calls);
    }

    public void addCall(final Call call) {
      InvariantChecks.checkNotNull(call);
      calls.add(call);
    }
  }

  protected static final class Mask {
    private final Collection<String> masks;

    public Mask(final String mask) {
      InvariantChecks.checkNotNull(mask);
      this.masks = Collections.singletonList(mask);
    }

    public Mask(final Collection<String> masks) {
      InvariantChecks.checkNotEmpty(masks);
      this.masks = masks;
    }

    public boolean isMatch(final BitVector value) {
      InvariantChecks.checkNotNull(value);

      final String text = value.toHexString();
      for (final String mask: masks) {
        if (testMask(mask, text)) {
          return true;
        }
      }

      return false;
    }

    private static boolean testMask(final String mask, final String value) {
      if (mask.length() != value.length()) {
        return false;
      }

      final int length = mask.length();
      for (int index = 0; index < length; ++index) {
        final char  maskCh =  mask.charAt(index);
        final char valueCh = value.charAt(index);

        if (maskCh != valueCh && maskCh != 'x' && maskCh != 'X') {
          return false;
        }
      }

      return true;
    }
  }

  protected static abstract class Argument {
    public static Argument newValue(
        final String name,
        final BigInteger value) {
      InvariantChecks.checkNotNull(value);
      return newCollection(name, Collections.singletonList(value));
    }

    public static Argument newCollection(
        final String name,
        final Collection<BigInteger> values) {
      return new ArgumentCollection(name, values);
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

  private static final class ArgumentCollection extends Argument {
    private final Collection<BigInteger> values;

    protected ArgumentCollection(
        final String name,
        final Collection<BigInteger> values) {
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
      return value.compareTo(from) >=0 && value.compareTo(to) <= 0;
    }
  }
}
