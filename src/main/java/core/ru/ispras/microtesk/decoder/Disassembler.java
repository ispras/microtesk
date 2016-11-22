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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.utils.FileUtils;

public final class Disassembler {
  public static boolean disassemble(
      final Options options,
      final String modelName,
      final String fileName) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(fileName);

    final Model model = loadModel(modelName);
    if (null == model) {
      return false;
    }

    final BinaryReader reader = newReader(fileName);
    if (null == reader) {
      return false;
    }

    final PrintWriter writer;
    try {
      writer = newWriter(
          options.hasValue(Option.OUTDIR) ? options.getValueAsString(Option.OUTDIR) :
                                            SysUtils.getHomeDir(),
          FileUtils.getShortFileNameNoExt(fileName),
          options.getValueAsString(Option.CODE_EXT)
          );
    } catch (final IOException e) {
      Logger.error("Failed to create output file. Reason: %s.", e.getMessage());
      return false;
    }

    try {
      decode(model.getDecoder(), reader, writer);
      return true;
    } finally {
      reader.close();
      writer.close();
    }
  }

  private static Model loadModel(final String modelName) {
    try {
      return SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error("Failed to load the %s model. Reason: %s.", modelName, e.getMessage());
      return null;
    }
  }

  private static BinaryReader newReader(final String fileName) {
    final File file = new File(fileName);
    if (!file.exists()) {
      Logger.error("The %s file does not exists.", fileName);
      return null;
    }

    try {
      return new BinaryReader(file);
    } catch (final IOException e) {
      Logger.error("Failed to open input file. Reason: %s.", e.getMessage());
      return null;
    }
  }

  private static PrintWriter newWriter(
      final String path,
      final String name,
      final String ext) throws IOException {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(ext);

    final File file = new File(path, name + "." + ext);
    final File fileParent = file.getParentFile();
    if (null != fileParent) {
      fileParent.mkdirs();
    }

    return new PrintWriter(new FileWriter(file));
  }

  private static void decode(
      final Decoder decoder,
      final BinaryReader reader,
      final PrintWriter writer) {
    InvariantChecks.checkNotNull(decoder);
    InvariantChecks.checkNotNull(reader);
    InvariantChecks.checkNotNull(writer);

    Logger.error("Dissambling is not currently supported.");
  }
}
