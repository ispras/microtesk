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
import java.util.Collection;
import java.util.List;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;

public final class PreparatorBuilder {
  private final MetaAddressingMode targetMetaData;
  private final boolean isComparator;

  private final LazyPrimitive target;
  private final LazyData data;
  private final List<Call> calls;

  private Preparator.Mask mask;
  private final List<Preparator.Argument> arguments;

  protected PreparatorBuilder(final MetaAddressingMode targetMetaData) {
    this(targetMetaData, false);
  }

  protected PreparatorBuilder(
      final MetaAddressingMode targetMetaData,
      final boolean isComparator) {
    checkNotNull(targetMetaData);

    this.targetMetaData = targetMetaData;
    this.isComparator = isComparator;

    final String targetName = targetMetaData.getName();

    this.target = new LazyPrimitive(Primitive.Kind.MODE, targetName, targetName);
    this.data = new LazyData();
    this.calls = new ArrayList<>();

    this.mask = null;
    this.arguments = new ArrayList<>();
  }

  public void setMaskValue(final String mask) {
    this.mask = new Preparator.Mask(mask);
  }

  public void setMaskCollection(final Collection<String> masks) {
    this.mask = new Preparator.Mask(masks);
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

  public String getTargetName() {
    return target.getName();
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

  public void addCall(final Call call) {
    checkNotNull(call);
    calls.add(call);
  }

  public Preparator build() {
    return new Preparator(isComparator, target, data, calls, mask, arguments);
  }
}
