package ru.ispras.microtesk.translator.mir;

import ru.ispras.castle.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class MirArchive {
  static final String MANIFEST = "manifest.json";

  private final Map<String, MirContext> mir = new java.util.HashMap<>();
  private JsonObject manifest;

  public static MirArchive open(final Path path) {
    final MirArchive arch = new MirArchive();
    try (final ZipFile zip = new ZipFile(path.toFile())) {
      final ZipEntry manifestEntry = zip.getEntry(MANIFEST);
      try (final JsonReader reader = Json.createReader(zip.getInputStream(manifestEntry))) {
        arch.manifest = reader.readObject();
      }
      for (final ZipEntry entry : Collections.list(zip.entries())) {
        if (!entry.getName().equals(MANIFEST)) {
          final MirParser parser = new MirParser(zip.getInputStream(entry));
          final MirContext ctx = parser.parse();
          arch.mir.put(ctx.name, ctx);
        }
      }
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
    return Collections.unmodifiableMap(mir);
  }
}
