/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.xml.XmlConstraintLoader;
import ru.ispras.fortress.solver.xml.XmlConstraintSaver;
import ru.ispras.fortress.solver.xml.XmlNotLoadedException;
import ru.ispras.fortress.solver.xml.XmlNotSavedException;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class SsaStorage {
  private SsaStorage() {}

  public static Map<String, SsaForm> load(final Path outPath, final String modelName) {
    InvariantChecks.checkNotNull(outPath);
    InvariantChecks.checkNotNull(modelName);

    final Path genPath = outPath.resolve("gen");
    final Path dirPath = genPath.resolve(modelName);

    final Map<String, SsaForm> ssa = new HashMap<>();
    try {
      final List<Constraint> constraints = new ArrayList<>();
      try (final DirectoryStream<Path> files = Files.newDirectoryStream(dirPath)) {
        for (final Path path : files) {
          final Constraint c = XmlConstraintLoader.load(Files.newInputStream(path));
          constraints.add(c);
        }
      }

      final SsaConverter converter = new SsaConverter(constraints);

      final Path indexPath = genPath.resolve(modelName + ".list");
      try (final BufferedReader reader =
            Files.newBufferedReader(indexPath, StandardCharsets.UTF_8)) {
        String line = reader.readLine();
        while (line != null) {
          ssa.put(line, converter.convert(line));
          line = reader.readLine();
        }
      }
    } catch (final DirectoryIteratorException e) {
      Logger.error("failed to load coverage model: " + e.getCause().toString());
      return Collections.emptyMap();
    } catch (final XmlNotLoadedException | IOException e ) {
      Logger.error("failed to load coverage model: " + e.toString());
      return Collections.emptyMap();
    }
    return ssa;
  }

  public static void store(
      final Path outPath,
      final String modelName,
      final Map<String, SsaForm> ssa) {
    InvariantChecks.checkNotNull(outPath);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(ssa);

    final Path genPath = outPath.resolve("gen");
    final Path dirPath = genPath.resolve(modelName);

    try {
      if (Files.notExists(dirPath)) {
        Files.createDirectories(dirPath);
      }

      final Path indexPath = genPath.resolve(modelName + ".list");
      try (final BufferedWriter writer =
            Files.newBufferedWriter(indexPath, StandardCharsets.UTF_8)) {
        final PrintWriter index = new PrintWriter(writer);

        for (final Map.Entry<String, SsaForm> entry : ssa.entrySet()) {
          index.println(entry.getKey());

          final Collection<Constraint> constraints =
            BlockConverter.convert(entry.getKey(), entry.getValue().getEntryPoint());
          for (final Constraint c : constraints) {
            final XmlConstraintSaver saver = new XmlConstraintSaver(c);
            final Path path = dirPath.resolve(c.getName() + ".xml");
            saver.save(Files.newOutputStream(path));
          }
        }
      }
    } catch (final XmlNotSavedException | IOException e) {
      Logger.error("failed to save coverage model: " + e.getMessage());
    }
  }

  public static Map<String, SsaForm> loadZip(final Path outPath, final String modelName) {
    InvariantChecks.checkNotNull(outPath);
    InvariantChecks.checkNotNull(modelName);

    final Path zipPath = outPath.resolve("gen").resolve(modelName + ".zip");
    final Map<String, SsaForm> ssa = new HashMap<>();

    try (final ZipFile zip = new ZipFile(zipPath.toFile())) {
      final List<? extends ZipEntry> entries = Collections.list(zip.entries());
      final List<Constraint> constraints = new ArrayList<>();

      for (final ZipEntry entry : entries) {
        if (!entry.isDirectory()) {
          final Constraint c = XmlConstraintLoader.load(zip.getInputStream(entry));
          constraints.add(c);
        }
      }
      final SsaConverter converter = new SsaConverter(constraints);
      for (final ZipEntry entry : entries) {
        if (entry.isDirectory()) {
          final String name = entry.getName().replaceAll("/", "");
          ssa.put(name, converter.convert(name));
        }
      }
    } catch (final XmlNotLoadedException | IOException e ) {
      Logger.error("failed to load coverage model: " + e.getMessage());
      return Collections.emptyMap();
    }
    return ssa;
  }

  public static void storeZip(
      final Path outPath,
      final String modelName,
      final Map<String, SsaForm> ssa) {
    InvariantChecks.checkNotNull(outPath);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(ssa);

    final Path genPath = outPath.resolve("gen");
    final Path archPath = genPath.resolve(modelName + ".zip");

    try {
      if (Files.notExists(genPath)) {
        Files.createDirectories(genPath);
      }
      try (final ZipOutputStream zip =
            new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(archPath)))) {
        for (final Map.Entry<String, SsaForm> entry : ssa.entrySet()) {
          zip.putNextEntry(new ZipEntry(entry.getKey() + "/"));
          zip.closeEntry();

          final Collection<Constraint> constraints =
            BlockConverter.convert(entry.getKey(), entry.getValue().getEntryPoint());
          for (final Constraint c : constraints) {
            final XmlConstraintSaver saver = new XmlConstraintSaver(c);
            final ZipEntry file =
              new ZipEntry(String.format("%s/%s.xml", entry.getKey(), c.getName()));
            zip.putNextEntry(file);
            saver.save(zip);
          }
        }
      }
    } catch (final XmlNotSavedException | IOException e) {
      Logger.error("failed to save coverage model: " + e.getMessage());
    }
  }
}
