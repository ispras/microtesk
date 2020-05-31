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

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.utils.Mask;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PreparatorBuilder
    implements CodeBlockBuilder<Preparator>, Delegator {
  private Where where;

  private final MetaAddressingMode targetMetaData;
  private final boolean isComparator;

  private final LazyPrimitive target;
  private final LazyData data;

  private String name;
  private Mask mask;
  private final List<Preparator.Argument> arguments;

  private final List<AbstractCall> calls;
  private final List<Preparator.Variant> variants;
  private Preparator.Variant currentVariant;

  protected PreparatorBuilder(
      final MetaAddressingMode targetMetaData,
      final boolean isComparator) {
    InvariantChecks.checkNotNull(targetMetaData);

    this.where = null;

    this.targetMetaData = targetMetaData;
    this.isComparator = isComparator;

    final String targetName = targetMetaData.getName();

    this.target = new LazyPrimitive(Primitive.Kind.MODE, targetName, targetName);
    this.data = new LazyData();

    this.name = null;
    this.mask = null;
    this.arguments = new ArrayList<>();

    this.calls = new ArrayList<>();
    this.variants = new ArrayList<>();
    this.currentVariant = null;
  }

  public void setWhere(final Where where) {
    InvariantChecks.checkNotNull(where);
    this.where = where;
  }

  public void setName(final String name) {
    InvariantChecks.checkNotNull(name);
    this.name = name;
  }

  public void setMaskValue(final String maskText) {
    InvariantChecks.checkNotNull(maskText);

    final Mask newMask = Mask.valueOf(maskText);
    if (null == newMask) {
      throw new IllegalArgumentException("Illegal mask format: " + maskText);
    }

    this.mask = newMask;
  }

  public void setMaskCollection(final Collection<String> maskTexts) {
    InvariantChecks.checkNotEmpty(maskTexts);

    final Mask newMask = Mask.valueOf(maskTexts);
    if (null == newMask) {
      throw new IllegalArgumentException("Illegal mask format: " + maskTexts);
    }

    this.mask = Mask.valueOf(maskTexts);
  }

  public void addArgumentValue(
      final String name,
      final BigInteger value) {
    checkArgumentDefined(name);
    this.arguments.add(Preparator.Argument.newValue(name, value));
  }

  public void addArgumentRange(
      final String name,
      final BigInteger from,
      final BigInteger to) {
    checkArgumentDefined(name);
    this.arguments.add(Preparator.Argument.newRange(name, from, to));
  }

  public void addArgumentCollection(
      final String name,
      final Collection<BigInteger> values) {
    checkArgumentDefined(name);
    this.arguments.add(Preparator.Argument.newCollection(name, values));
  }

  private void checkArgumentDefined(final String name) {
    final MetaArgument metaArgument = targetMetaData.getArgument(name);
    if (null == metaArgument) {
      throw new IllegalArgumentException(String.format(
          "The %s argument is not defined for the %s addressing mode.", name, getTargetName()));
    }
  }

  public void beginVariant(final String name, final int bias) {
    beginVariant(new Preparator.Variant(name, bias));
  }

  public void beginVariant(final String name) {
    beginVariant(new Preparator.Variant(name));
  }

  private void beginVariant(final Preparator.Variant variant) {
    InvariantChecks.checkTrue(null == currentVariant);

    if (!calls.isEmpty()) {
      throw new IllegalStateException(
          "Cannot add a variant to a preparator when it defines calls in the global space.");
    }

    variants.add(variant);
    currentVariant = variant;
  }

  public void endVariant() {
    InvariantChecks.checkFalse(null == currentVariant);
    currentVariant = null;
  }

  @Override
  public void addCall(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    if (null != currentVariant) {
      currentVariant.addCall(call);
    } else {
      if (!variants.isEmpty()) {
        throw new IllegalStateException(
            "Cannot add calls to the global space of a preparator when it defines variants.");
      }

      calls.add(call);
    }
  }

  @Override
  public LazyValue delegateValue() {
    return newValue();
  }

  @Override
  public LazyValue delegateValue(int start, int end) {
    return newValue(start, end);
  }

  public LazyValue newValue() {
    return new LazyValue(data);
  }

  public LazyValue newValue(final int start, final int end) {
    return new LazyValue(data, start, end);
  }

  public Primitive getTarget() {
    return target;
  }

  public String getTargetName() {
    return target.getName();
  }

  @Override
  public Preparator build() {
    return new Preparator(
        where,
        isComparator,
        target,
        data,
        name,
        mask,
        arguments,
        calls,
        variants
        );
  }
}
