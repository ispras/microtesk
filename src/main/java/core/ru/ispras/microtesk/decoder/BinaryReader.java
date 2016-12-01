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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public final class BinaryReader {
  private final InputStream inputStream;
  private boolean open;

  private final byte[] buffer = new byte[1024];
  private int position = buffer.length;

  public BinaryReader(final File file) throws IOException {
    InvariantChecks.checkNotNull(file);

    this.inputStream = new FileInputStream(file);
    this.open = true;
  }

  public BitVector read(final int byteSize) {
    InvariantChecks.checkBounds(byteSize, buffer.length);

    if (position + byteSize > buffer.length) {
      try {
        final int bytesRead = inputStream.read(buffer, 0, buffer.length);
        if (bytesRead <= 0) {
          return null;
        }
        position = 0;
      } catch (final IOException e) {
        throw new IllegalStateException(e);
      }
    }

    final byte[] bytes = Arrays.copyOfRange(buffer, position, position + byteSize);
    final BitVector data = BitVector.valueOf(bytes, byteSize * 8);

    position += byteSize;
    return data;
  }

  public void close() {
    if (!open) {
      return;
    }

    if (null != inputStream) {
      try {
        inputStream.close();
        open = false;
      } catch (final IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  public void retreat(final int byteSize) {
    InvariantChecks.checkBoundsInclusive(byteSize, position);
    position -= byteSize;
  }
}
