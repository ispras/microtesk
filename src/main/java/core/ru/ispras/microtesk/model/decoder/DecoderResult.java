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

import ru.ispras.microtesk.model.IsaPrimitive;

/**
 * {@link DecoderResult} class holds the result of primitive decoding.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class DecoderResult {
  private final IsaPrimitive primitive;
  private final int bitSize;

  public DecoderResult(
      final IsaPrimitive primitive,
      final int bitSize) {
    this.primitive = primitive;
    this.bitSize = bitSize;
  }

  public IsaPrimitive getPrimitive() {
    return primitive;
  }

  public int getBitSize() {
    return bitSize;
  }
}
