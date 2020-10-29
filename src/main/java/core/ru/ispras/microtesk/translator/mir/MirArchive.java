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

import ru.ispras.castle.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class MirArchive {
  static final String MANIFEST = "manifest.json";

  private Map<String, MirContext> mir;
  private JsonObject manifest;

  public static MirArchive open(final Path path) {
    final MirArchive arch = new MirArchive();
    try {
      // TODO enforce ZIP resource deallocation (close() it)
      final ZipFile zip = new ZipFile(path.toFile());
      final ZipEntry manifestEntry = zip.getEntry(MANIFEST);
      try (final JsonReader reader = Json.createReader(zip.getInputStream(manifestEntry))) {
        arch.manifest = reader.readObject();
      }
      arch.mir = new MirLibrary(zip);
    } catch (final IOException e) {
      Logger.warning("MirArchive: failed to load '%s': %s", path.toString(), e.toString());
    }
    return arch;
  }

  public JsonObject getManifest() {
    return manifest;
  }

  public MirContext load(final String name) {
    return mir.get(name);
  }

  public Map<String, MirContext> loadAll() {
    // return Collections.unmodifiableMap(mir);
    return mir;
  }

  static final class MirLibrary extends AbstractMap<String, MirContext> {
    private static final String SUFFIX = ".mir";

    private final ZipFile zip;
    private final Set<Map.Entry<String, MirContext>> entrySet;

    MirLibrary(final ZipFile zip) {
      this.zip = zip;

      final var entries = new java.util.HashSet<Map.Entry<String, MirContext>>();
      for (final ZipEntry entry : Collections.list(zip.entries())) {
        final String name = entry.getName();
        if (name.endsWith(SUFFIX)) {
          entries.add(new Entry(name.substring(0, name.length() - SUFFIX.length())));
        }
      }
      this.entrySet = Collections.unmodifiableSet(entries);
    }

    @Override
    public Set<Map.Entry<String, MirContext>> entrySet() {
      return entrySet;
    }

    private final class Entry implements Map.Entry<String, MirContext> {
      private final String key;

      Entry(final String key) {
        this.key = key;
      }

      @Override
      public String getKey() {
        return key;
      }

      @Override
      public MirContext getValue() {
        final var entry = zip.getEntry(key + SUFFIX);
        try {
          final var parser = new MirParser(zip.getInputStream(entry));
          return parser.parse();
        } catch (final IOException e) {
          throw new IllegalStateException(
            String.format("Unable to load MIR '%s'", entry.getName()), e);
        }
      }

      @Override
      public MirContext setValue(final MirContext value) {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean equals(final Object that) {
        return this == that;
      }

      @Override
      public int hashCode() {
        return key.hashCode();
      }
    }
  }
}
