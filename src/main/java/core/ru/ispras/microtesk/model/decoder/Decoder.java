/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.util.InvariantChecks;

public abstract class Decoder {
  private final int maxImageSize;
  private final boolean imageSizeFixed;
  private final BitVector opc;
  private final BitVector opcMask;

  protected Decoder(
      final int maxImageSize,
      final boolean imageSizeFixed,
      final String opc,
      final String opcMask) {
    this.maxImageSize = maxImageSize;
    this.imageSizeFixed = imageSizeFixed;

    this.opc = null != opc ? BitVector.valueOf(opc, 2, opc.length()) : null;
    this.opcMask = null != opcMask ? BitVector.valueOf(opcMask, 2, opcMask.length()) : null;
  }

  public final int getMaxImageSize() {
    return maxImageSize;
  }

  public final boolean isImageSizeFixed() {
    return imageSizeFixed;
  }

  public final BitVector getOpc() {
    return opc;
  }

  public final BitVector getOpcMask() {
    return opcMask;
  }

  public final boolean isOpcMatch(final BitVector image) {
    if (null == opc) {
      return true;
    }

    final BitVector imageOpc = applyOpcMask(image);
    return imageOpc.equals(opc);
  }

  protected final BitVector applyOpcMask(final BitVector image) {
    InvariantChecks.checkTrue(opcMask.getBitSize() == image.getBitSize());
    return BitVectorMath.and(image, opcMask);
  }

  public abstract DecoderResult decode(BitVector image);
}
