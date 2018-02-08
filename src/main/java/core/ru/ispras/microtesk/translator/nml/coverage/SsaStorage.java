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
import ru.ispras.microtesk.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

final class SsaStorage {
  private SsaStorage() {}

  public static Map<String, SsaForm> load(final String model, final String path) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(path);

    final String dirName = String.format("%s%sgen", path, File.separator);
    final File dir = new File(String.format("%s%s%s", dirName, File.separator, model));
    final Collection<Constraint> constraints = new ArrayList<>();

    for (final File file : dir.listFiles()) {
      try {
        constraints.add(XmlConstraintLoader.loadFromFile(file.getPath()));
      } catch (final XmlNotLoadedException e) {
        System.err.println(e.getMessage());
      }
    }

    final SsaConverter converter = new SsaConverter(constraints);
    final Map<String, SsaForm> ssa = new HashMap<>();

    try {
      final BufferedReader reader = new BufferedReader(
          new FileReader(String.format("%s%s%s.list", dirName, File.separator, model)));

      String line = reader.readLine();
      while (line != null) {
        ssa.put(line, converter.convert(line));
        line = reader.readLine();
      }

      reader.close();
    } catch (final IOException e) {
      System.err.println(e.getMessage());
    }

    return ssa;
  }

  public static void store(
      final String targetDir,
      final String modelName,
      final Map<String, SsaForm> ssa) {
    InvariantChecks.checkNotNull(targetDir);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(ssa);

    try {
      final File genDir = new File(FileUtils.getNormalizedPath(targetDir));

      if (!genDir.exists()) {
        genDir.mkdirs();
      }

      final PrintWriter out = new PrintWriter(
          new BufferedWriter(new FileWriter(
              String.format("%s%s%s.list", genDir.getCanonicalPath(), File.separator, modelName))));

      for (final Map.Entry<String, SsaForm> entry : ssa.entrySet()) {
        out.println(entry.getKey());
        for (final Constraint c :
          BlockConverter.convert(entry.getKey(), entry.getValue().getEntryPoint())) {

          final XmlConstraintSaver saver = new XmlConstraintSaver(c);

          try {
            final File path = new File(
                String.format("%s%s%s", genDir.getCanonicalPath(), File.separator, modelName));

            path.mkdir();
            saver.saveToFile(
                String.format("%s%s%s.xml", path.getPath(), File.separator, c.getName()));
          } catch (final XmlNotSavedException e) {
            Logger.error("Failed to save coverage model to the XML file: %s", e.getMessage());
          }
        }
      }

      out.flush();
      out.close();
    } catch (final java.io.IOException e) {
      Logger.error("Failed to save coverage model to the file system: %s", e.getMessage());
    }
  }
}
