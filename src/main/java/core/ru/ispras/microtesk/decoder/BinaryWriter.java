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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

public final class BinaryWriter {
  private final File file;
  private final OutputStream outputStream;
  private boolean open;

  private final byte[] buffer = new byte[1024];
  private int position = 0;

  public BinaryWriter(final String fileName) throws IOException {
    InvariantChecks.checkNotNull(fileName);

    this.file = new File(fileName);
    if (null != file.getParentFile()) {
      file.mkdirs();
    }

    this.outputStream = new FileOutputStream(file);
    this.open = true;
  }

  public void write(final BitVector data) throws IOException {
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkTrue(open);

    if (position + data.getByteSize() > buffer.length) {
      flush();
    }

    for (int index = 0; index < data.getByteSize(); ++index) {
      buffer[position++] = data.getByte(index);
    }
  }

  public void close() throws IOException {
    if (open) {
      flush();
      outputStream.close();
      open = false;
    }
  }

  public void delete() {
    if (file.exists()) {
      file.delete();
    }
  }

  private void flush() throws IOException {
    InvariantChecks.checkTrue(open);
    outputStream.write(buffer, 0, position);
    position = 0;
  }
}
