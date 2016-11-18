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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public abstract class DecoderGroup implements Decoder {
  private final List<Decoder> decoders = new ArrayList<>();
  private final int maxImageSize;
  private final boolean imageSizeFixed;

  protected DecoderGroup(final int maxImageSize, final boolean imageSizeFixed) {
    this.maxImageSize = maxImageSize;
    this.imageSizeFixed = imageSizeFixed;
  }

  protected final void add(final Decoder decoder) {
    InvariantChecks.checkNotNull(decoder);
    InvariantChecks.checkTrue(maxImageSize >= decoder.getMaxImageSize());
    InvariantChecks.checkTrue(imageSizeFixed ? decoder.isImageSizeFixed() : true);
    decoders.add(decoder);
  }

  @Override
  public final int getMaxImageSize() {
    return maxImageSize;
  }

  @Override
  public final boolean isImageSizeFixed() {
    return imageSizeFixed;
  }

  @Override
  public final BitVector getOpc() {
    return null;
  }

  @Override
  public final BitVector getOpcMask() {
    return null;
  }

  @Override
  public DecoderResult decode(final BitVector image) {
    for (final Decoder decoder : decoders) {
      final DecoderResult result = decoder.decode(image);
      if (null != result) {
        return result;
      }
    }

    return null;
  }
}
