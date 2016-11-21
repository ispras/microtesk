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

public final class Disassembler {
  public static void disassemble(
      final Options options,
      final String modelName,
      final String fileName) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(fileName);

    final Model model;
    try {
      model = SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error(e.getMessage());
      Logger.error("Failed to load the %s model. Dissambling is aborted.", modelName);
      return;
    }

    final File source = new File(fileName);
    if (source.exists()) {
      Logger.error("The %s file does not exists. Dissambling is aborted.", fileName);
      return;
    }

    try {
      newFile(
          options.hasValue(Option.OUTDIR) ? options.getValueAsString(Option.OUTDIR) :
                                            SysUtils.getHomeDir(),
          options.getValueAsString(Option.CODE_PRE),
          options.getValueAsString(Option.CODE_EXT)
          );
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Logger.message(model.getName());
    Logger.error("Dissambling is not currently supported.");
  }

  /*public static void copyFile(final File source, final File target) throws IOException {
    try (final InputStream in = new FileInputStream(source);
         final OutputStream out = new FileOutputStream(target)) {
      final byte[] buf = new byte[1024];
      int length;
      while ((length = in.read(buf)) > 0) {
        
      }
    }
  }*/

  private static PrintWriter newFile(
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
}
