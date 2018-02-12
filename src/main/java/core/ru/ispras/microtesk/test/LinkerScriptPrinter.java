/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.Section;
import ru.ispras.microtesk.model.memory.Sections;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.generation.STFileGenerator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

final class LinkerScriptPrinter {
  private final String fileName;
  private final String fileFullName;

  private static final String[] LINKER_SCRIPT_TEMPLATE =
      new String[] { PackageInfo.COMMON_TEMPLATE_DIR + "LinkerScript.stg" };

  public LinkerScriptPrinter(final Options options) {
    InvariantChecks.checkNotNull(options);

    final String directory = Printer.getOutDir(options);
    this.fileName = options.getValueAsString(Option.CODE_PRE) + ".ld";
    this.fileFullName = new File(directory, fileName).getPath();
  }

  public String getFileName() {
    return fileName;
  }

  public void print() throws IOException {
    final STFileGenerator fileGenerator =
      new STFileGenerator(fileFullName, LINKER_SCRIPT_TEMPLATE, new STBuilder() {

          @Override
          public ST build(final STGroup group) {
            final ST st = group.getInstanceOf("linker_script");
            st.add("time", new Date().toString());

            final Section textSection = Sections.get().getTextSection();
            st.add("text_id", textSection.getName());
            st.add("text_va", toHexString(textSection.getBaseVa()));

            final Section dataSection = Sections.get().getDataSection();
            st.add("data_id", dataSection.getName());
            st.add("data_va", toHexString(dataSection.getBaseVa()));

            for (final Section section : Sections.get().getSectionsOrderedByVa()) {
              if (section != textSection && section != dataSection) {
                st.add("section_ids", section.getName());
                st.add("section_vas", toHexString(section.getBaseVa()));
              }
            }

            return st;
          }
    });

    fileGenerator.generate();
  }

  private static String toHexString(final BigInteger value) {
    return String.format("0x%04X", value);
  }
}
