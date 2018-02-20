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

package ru.ispras.microtesk.translator.nml.generation.decoder;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.FileGenerator;
import ru.ispras.microtesk.codegen.STBuilder;
import ru.ispras.microtesk.codegen.STFileGenerator;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.IrVisitorDefault;
import ru.ispras.microtesk.translator.nml.ir.IrWalker;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public final class DecoderGenerator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private Ir ir;

  public DecoderGenerator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/java";
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;

    generatePrimitives();
    generateDecoder();
  }

  private void generatePrimitives() {
    InvariantChecks.checkNotNull(ir);
    final IrWalker walker = new IrWalker(ir);
    final Visitor visitor = new Visitor();
    walker.visit(visitor, IrWalker.Direction.LINEAR);
  }

  private void generateDecoder() {
    InvariantChecks.checkNotNull(ir);

    List<Primitive> roots = Collections.emptyList();
    for (final Primitive primitive : ir.getRoots()) {
      if (primitive.getName().equalsIgnoreCase("instruction")) {
        roots = Collections.singletonList(primitive);
        break;
      }
    }

    generateFile(null, new STBDecoderGroup(getModelName(), roots));
  }

  private final class Visitor extends IrVisitorDefault {
    @Override
    public void onPrimitiveBegin(final Primitive item) {
      if (null == item.getInfo().getImageInfo()) {
        return;
      }

      if (item.isOrRule()) {
        generateFile(item.getName(), new STBDecoderGroup(getModelName(), (PrimitiveOR) item));
      } else {
        generateFile(item.getName(), new STBDecoder(getModelName(), (PrimitiveAND) item));
      }
    }
  }

  private void generateFile(
      final String className,
      final STBuilder templateBuilder) {
    final String[] templateGroups = new String[] {
        PackageInfo.COMMON_TEMPLATE_DIR + "JavaCommon.stg",
        PackageInfo.NML_TEMPLATE_DIR + "Decoder.stg"
        };

    final FileGenerator generator = new STFileGenerator(
        getFileName(className), templateGroups, templateBuilder);

    try {
      generator.generate();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileName(final String className) {
    InvariantChecks.checkNotNull(ir);
    return String.format(
        "%s/%s/decoder/%s.java",
        PackageInfo.getModelOutDir(getOutDir()),
        getModelName(),
        getDecoderName(className)
        );
  }

  private String getModelName() {
    InvariantChecks.checkNotNull(ir);
    return ir.getModelName();
  }

  public static String getDecoderName(final String name) {
    final StringBuilder sb = new StringBuilder("Decoder");

    if (null != name && !name.isEmpty()) {
      sb.append(Character.toUpperCase(name.charAt(0)));
      if (name.length() > 1) {
        sb.append(name.substring(1, name.length()));
      }
    }

    return sb.toString();
  }
}
