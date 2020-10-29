/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class ArchiveWriter implements Closeable {
  public ArchiveWriter(final Path path) throws IOException {
    this.zip = new ZipOutputStream(Files.newOutputStream(path));
    this.writer = new java.io.OutputStreamWriter(zip, "UTF-8");
  }

  public Writer newText(final String name) throws IOException {
    return new TextWriter(name);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  private final ZipOutputStream zip;
  private final Writer writer;

  private final class TextWriter extends Writer {
    private ZipEntry entry;

    TextWriter(final String name) throws IOException {
      this.entry = new ZipEntry(name);
      zip.putNextEntry(entry);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
      if (entry != null) {
        writer.write(cbuf, off, len);
      } else {
        throw new IOException();
      }
    }

    @Override
    public void flush() throws IOException {
      if (entry != null) {
        writer.flush();
      } else {
        throw new IOException();
      }
    }

    @Override
    public void close() throws IOException {
      if (entry != null) {
        flush();
        zip.closeEntry();
        entry = null;
      }
    }
  }
}
