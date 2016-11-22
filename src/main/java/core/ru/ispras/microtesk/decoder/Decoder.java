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

package ru.ispras.microtesk.decoder;

import ru.ispras.fortress.data.types.bitvector.BitVector;

public abstract class Decoder {
  private final int maxImageSize;
  private final boolean imageSizeFixed;
  private final BitVector opc;
  private final BitVector opcMask;

  protected Decoder(
      final int maxImageSize,
      final boolean imageSizeFixed,
      final BitVector opc,
      final BitVector opcMask) {
    this.maxImageSize = maxImageSize;
    this.imageSizeFixed = imageSizeFixed;
    this.opc = opc;
    this.opcMask = opcMask;
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

  public abstract DecoderResult decode(BitVector image);
}
