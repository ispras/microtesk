/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.template.DataGenerator;
import ru.ispras.microtesk.test.template.FixedValue;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.Value;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link DirectiveFactory} implements a configurable factory for creating data directives.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DirectiveFactory {
  private final Options options;
  private final Map<String, DirectiveTypeInfo> types;
  private final int addressableUnitBitSize;
  private final int maxTypeBitSize;

  private DirectiveFactory(
      final Options options,
      final Map<String, DirectiveTypeInfo> types,
      final int addressableUnitBitSize,
      final int maxTypeBitSize) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(types);

    this.options = options;
    this.types = types;
    this.addressableUnitBitSize = addressableUnitBitSize;
    this.maxTypeBitSize = maxTypeBitSize;
  }

  public static final class Builder {
    private final Options options;
    private final boolean isDebugPrinting;
    private final int addressableUnitBitSize;

    private final Map<String, DirectiveTypeInfo> types;
    private int maxTypeBitSize;

    public Builder(final Options options, final int addressableUnitBitSize) {
      InvariantChecks.checkNotNull(options);
      InvariantChecks.checkGreaterThanZero(addressableUnitBitSize);

      this.options = options;
      this.isDebugPrinting = options.getValueAsBoolean(Option.DEBUG_PRINT);
      this.addressableUnitBitSize = addressableUnitBitSize;

      this.types = new HashMap<>();
      this.maxTypeBitSize = 0;
    }

    public void defineType(
        final String id,
        final String text,
        final String typeName,
        final int[] typeArgs,
        final String format,
        final boolean align) {
      InvariantChecks.checkNotNull(id);
      InvariantChecks.checkNotNull(text);
      InvariantChecks.checkNotNull(typeName);
      InvariantChecks.checkNotNull(typeArgs);
      InvariantChecks.checkNotNull(format);

      final Type type = Type.typeOf(typeName, typeArgs);
      debug("Defining %s as %s ('%s%s')...", type, id, text, format.isEmpty() ? "" : " " + format);

      types.put(id, new DirectiveTypeInfo(type, text, format, align));
      maxTypeBitSize = Math.max(maxTypeBitSize, type.getBitSize());
    }

    public DirectiveFactory build() {
      return new DirectiveFactory(
          options,
          types,
          addressableUnitBitSize,
          maxTypeBitSize);
    }

    private void debug(final String format, final Object... args) {
      if (isDebugPrinting) {
        Logger.debug(format, args);
      }
    }
  }

  public final class DataValueBuilder {
    private final DirectiveTypeInfo type;
    private final List<Value> values;
    private final boolean align;

    private DataValueBuilder(final DirectiveTypeInfo type, final boolean align) {
      InvariantChecks.checkNotNull(type);

      this.type = type;
      this.values = new ArrayList<>();
      this.align = align;
    }

    public void add(final BigInteger value) {
      InvariantChecks.checkNotNull(value);
      values.add(new FixedValue(value));
    }

    public void add(final Value value) {
      InvariantChecks.checkNotNull(value);
      values.add(value);
    }

    public void addDouble(final double value) {
      if (type.type.getBitSize() == 32) {
        add(BigInteger.valueOf(Float.floatToIntBits((float) value)));
      } else {
        add(BigInteger.valueOf(Double.doubleToLongBits(value)));
      }
    }

    public Directive build() {
      return newDataValues(type, values, align);
    }
  }

  public DataValueBuilder getDataValueBuilder(final String typeName, final boolean align) {
    final DirectiveTypeInfo type = findTypeInfo(typeName);
    return new DataValueBuilder(type, align);
  }

  public DataValueBuilder getDataValueBuilder(final int typeBitSize, final boolean align) {
    final DirectiveTypeInfo type = findTypeInfo(typeBitSize, align);
    return new DataValueBuilder(type, align);
  }

  public Directive newText(final String text) {
    return new DirectiveText(options, text);
  }

  public Directive newComment(final String text) {
    return new DirectiveComment(options, text);
  }

  public Directive newLabel(final LabelValue label) {
    return new DirectiveLabel(options, label);
  }

  public Directive newGlobalLabel(final LabelValue label) {
    return new DirectiveLabelGlobal(options, label);
  }

  public Directive newWeakLabel(final LabelValue label) {
    return new DirectiveLabelWeak(options, label);
  }

  public Directive newOption(final String option) {
    return new DirectiveOption(options, option);
  }

  public Directive newOrigin(final BigInteger origin) {
    return new DirectiveOrigin(options, origin);
  }

  public Directive newOriginAbsolute(final BigInteger origin) {
    return new DirectiveOriginAbsolute(options, origin);
  }

  public Directive newOriginRelative(final BigInteger delta) {
    return new DirectiveOriginRelative(options, delta);
  }

  public Directive newAlign(
      final int alignment, final int alignmentInBytes) {
    return new DirectiveAlign(options, alignment, alignmentInBytes);
  }

  public Directive newAlignByte(final int alignment) {
    return new DirectiveAlignByte(options, alignment);
  }

  public Directive newAlignPower2(
      final int alignment, final int alignmentInBytes) {
    return new DirectiveAlignPower2(options, alignment, alignmentInBytes);
  }

  public Directive newSpace(
      final String text,
      final int data,
      final int length) {
    return new DirectiveSpace(options, text, BitVector.valueOf(data, addressableUnitBitSize), length);
  }

  public Directive newStrings(
      final String text,
      final boolean zeroTerm,
      final String[] strings) {
    return new DirectiveString(options, text, zeroTerm, strings);
  }

  public Directive newData(
      final String typeName,
      final BigInteger[] values,
      final boolean align) {
    final DirectiveTypeInfo typeInfo = findTypeInfo(typeName);
    return newData(typeInfo, values, align);
  }

  public Directive newData(
      final DirectiveTypeInfo typeInfo,
      final BigInteger[] values,
      final boolean align) {
    InvariantChecks.checkNotNull(typeInfo);
    InvariantChecks.checkNotEmpty(values);

    final List<BitVector> valueList = new ArrayList<>(values.length);
    for (final BigInteger value : values) {
      final BitVector data = BitVector.valueOf(value, typeInfo.type.getBitSize());
      valueList.add(data);
    }

    return new DirectiveDataConst(options, typeInfo.text, valueList, align);
  }

  public Directive newData(
      final String typeName,
      final DataGenerator generator,
      final int count,
      final boolean align) {
    final DirectiveTypeInfo typeInfo = findTypeInfo(typeName);
    return newData(typeInfo, generator, count, align);
  }

  public Directive newData(
      final DirectiveTypeInfo typeInfo,
      final DataGenerator generator,
      final int count,
      final boolean align) {
    InvariantChecks.checkNotNull(typeInfo);
    InvariantChecks.checkNotNull(generator);
    InvariantChecks.checkGreaterThanZero(count);

    final List<BitVector> values = new ArrayList<>(count);
    for (int index = 0; index < count; index++) {
      values.add(generator.nextData());
    }

    return new DirectiveDataConst(options, typeInfo.text, values, align);
  }

  public Directive newDataValues(
      final String typeName,
      final List<Value> values,
      final boolean align) {
    final DirectiveTypeInfo typeInfo = findTypeInfo(typeName);
    return newDataValues(typeInfo, values, align);
  }

  public Directive newDataValues(
      final DirectiveTypeInfo typeInfo,
      final List<Value> values,
      final boolean align) {
    return new DirectiveDataValue(options, typeInfo, values, align);
  }

  public int getMaxTypeBitSize() {
    return maxTypeBitSize;
  }

  public DirectiveTypeInfo findTypeInfo(final String typeName) {
    InvariantChecks.checkNotNull(typeName);
    final DirectiveTypeInfo typeInfo = types.get(typeName);

    if (null == typeInfo) {
      throw new GenerationAbortedException(
          String.format("The %s data type is not defined.", typeName));
    }

    return typeInfo;
  }

  public DirectiveTypeInfo findTypeInfo(final int typeSizeInBits, final boolean align) {
    InvariantChecks.checkGreaterThanZero(typeSizeInBits);

    for (final DirectiveTypeInfo typeInfo : types.values()) {
      if (typeSizeInBits == typeInfo.type.getBitSize() && (!align || typeInfo.align)) {
        return typeInfo;
      }
    }

    throw new GenerationAbortedException(
        String.format("No %d-bit type is defined.", typeSizeInBits));
  }
}
