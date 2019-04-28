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
