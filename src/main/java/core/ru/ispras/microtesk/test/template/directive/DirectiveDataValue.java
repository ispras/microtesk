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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.microtesk.utils.FormatMarker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class DirectiveDataValue extends Directive {
  private final DirectiveTypeInfo typeInfo;
  private final List<Value> values;
  private final boolean align;

  DirectiveDataValue(
      final Options options,
      final DirectiveTypeInfo typeInfo,
      final List<Value> values,
      final boolean align) {
    super(options);

    InvariantChecks.checkNotNull(typeInfo);
    InvariantChecks.checkNotEmpty(values);

    this.typeInfo = typeInfo;
    this.values = values;
    this.align = align;
  }

  @Override
  public Kind getKind() {
    return Kind.DATA;
  }

  private BitVector toBitVector(final Value value) {
    return BitVector.valueOf(value.getValue(), typeInfo.type.getBitSize());
  }

  @Override
  public String getText() {
    final StringBuilder sb = new StringBuilder(typeInfo.text);

    boolean isFirst = true;
    for (final Value value : values) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(',');
      }
      sb.append(' ');

      final Data data = Data.valueOf(typeInfo.type, value.getValue());
      if (typeInfo.format.isEmpty()) {
        sb.append("0x");
        sb.append(data.toHexString());
      } else if (typeInfo.formatMarker.isKind(FormatMarker.Kind.STR)) {
        sb.append(String.format(typeInfo.format, data.toString()));
      } else if (typeInfo.formatMarker.isKind(FormatMarker.Kind.HEX)) {
        sb.append(String.format(typeInfo.format, data.bigIntegerValue(false)));
      } else {
        sb.append(String.format(typeInfo.format, data.bigIntegerValue()));
      }
    }

    return sb.toString();
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    BigInteger current = currentAddress;

    for (final Value value : values) {
      current = allocator.allocate(current, toBitVector(value), align).second;
    }

    return current;
  }

  @Override
  public Directive copy() {
    final List<Value> newValues = new ArrayList<>(values.size());
    for (final Value value : values) {
      newValues.add(value.copy());
    }
    return new DirectiveDataValue(options, typeInfo, newValues, align);
  }
}
