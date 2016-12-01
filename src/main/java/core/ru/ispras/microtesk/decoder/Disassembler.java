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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.api.IsaPrimitive;
import ru.ispras.microtesk.model.api.Model;
import ru.ispras.microtesk.model.api.TemporaryVariables;
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

    final String fileExt = options.getValueAsString(Option.CODE_EXT);
    final String fileShortName = FileUtils.getShortFileNameNoExt(fileName);
    final PrintWriter writer = newWriter(getOutDir(options), fileShortName, fileExt);
    if (null == writer) {
      reader.close();
      return false;
    }

    try {
      return decode(model, reader, writer);
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

  private static String getOutDir(final Options options) {
    return options.hasValue(Option.OUTDIR) ?
        options.getValueAsString(Option.OUTDIR) : SysUtils.getHomeDir();
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
      final String ext) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(ext);

    final File file = new File(path, name + "." + ext);
    final File fileParent = file.getParentFile();
    if (null != fileParent) {
      fileParent.mkdirs();
    }

    try {
      return new PrintWriter(new FileWriter(file));
    } catch (final IOException e) {
      Logger.error("Failed to create output file. Reason: %s.", e.getMessage());
      return null;
    }
  }

  private static boolean decode(
      final Model model,
      final BinaryReader reader,
      final PrintWriter writer) {
    InvariantChecks.checkNotNull(model);
    InvariantChecks.checkNotNull(reader);
    InvariantChecks.checkNotNull(writer);

    final Decoder decoder = model.getDecoder();
    final TemporaryVariables tempVars = model.getTempVars();

    final int maxImageSize = decoder.getMaxImageSize();
    final boolean imageSizeFixed = decoder.isImageSizeFixed();

    InvariantChecks.checkTrue(0 == maxImageSize % 8);
    final int byteSize = maxImageSize / 8;

    BitVector data = null;
    while ((data = reader.read(byteSize)) != null) {
      final DecoderResult result = decoder.decode(data);
      if (null == result) {
        Logger.error("Unrecognized instructions encoding: %d'b%s", data.getBitSize(), data);
        return false;
      }

      final IsaPrimitive primitive = result.getPrimitive();
      final String text = primitive.syntax(tempVars);

      Logger.debug(text);
      writer.println(text);

      if (!imageSizeFixed) {
        final int bitsRead = result.getBitSize();
        InvariantChecks.checkTrue(0 == bitsRead % 8);

        final int bytesRead = bitsRead / 8;
        reader.retreat(byteSize - bytesRead);
      }
    }

    return true;
  }
}
