/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.decoder;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;

public abstract class DecoderItem extends Decoder {
  private int position = 0;

  protected DecoderItem(
      final int maxImageSize,
      final boolean imageSizeFixed,
      final String opc,
      final String opcMask) {
    super(maxImageSize, imageSizeFixed, opc, opcMask);
  }

  protected final void resetPosition() {
    position = 0;
  }

  protected final boolean matchNextOpc(final BitVector image, final BitVector value) {
    final int newPosition = position + value.getBitSize();
    final BitVector field = field(image, position, newPosition - 1);

    if (!field.equals(value)) {
      return false;
    }

    position = newPosition;
    return true;
  }

  protected final Immediate readNextImmediate(final BitVector image, final Type type) {
    final int newPosition = position + type.getBitSize();
    final BitVector field = field(image, position, newPosition - 1);

    position = newPosition;
    return new Immediate(new Data(type, field));
  }

  protected final IsaPrimitive readNextPrimitive(final BitVector image, final Decoder decoder) {
    final BitVector field =
        field(image, position, position + decoder.getMaxImageSize() - 1);

    final DecoderResult result = decoder.decode(field);
    if (null == result) {
      return null;
    }

    position += result.getBitSize();
    return result.getPrimitive();
  }

  protected final DecoderResult newResult(final IsaPrimitive primitive) {
    return new DecoderResult(primitive, position);
  }

  private static BitVector field(final BitVector vector, final int start, final int end) {
    final int offset = vector.getBitSize() - 1;
    return vector.field(offset - start, offset - end);
  }
}
