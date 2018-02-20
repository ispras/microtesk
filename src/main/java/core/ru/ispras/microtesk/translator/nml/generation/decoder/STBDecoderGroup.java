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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.STBuilder;
import ru.ispras.microtesk.model.decoder.DecoderGroup;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.ImageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

import java.util.ArrayList;
import java.util.List;

final class STBDecoderGroup implements STBuilder {
  private final String modelName;
  private final String name;
  private final ImageInfo imageInfo;
  private final List<String> items;

  public STBDecoderGroup(final String modelName, final PrimitiveOR group) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkNotNull(group.getInfo().getImageInfo());

    this.modelName = modelName;
    this.name = DecoderGenerator.getDecoderName(group.getName());
    this.imageInfo = group.getInfo().getImageInfo();
    this.items = new ArrayList<>();

    for (final Primitive primitive : group.getOrs()) {
      if (null != primitive.getInfo().getImageInfo()) {
        this.items.add(DecoderGenerator.getDecoderName(primitive.getName()));
      }
    }
  }

  public STBDecoderGroup(final String modelName, final List<Primitive> roots) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(roots);

    this.modelName = modelName;
    this.name = DecoderGenerator.getDecoderName(null);
    this.items = new ArrayList<>();

    ImageInfo info = new ImageInfo(0, true);
    boolean isFirst = true;

    for (final Primitive primitive : roots) {
      final ImageInfo primitiveImageInfo = primitive.getInfo().getImageInfo();
      if (null != primitiveImageInfo) {
        items.add(DecoderGenerator.getDecoderName(primitive.getName()));
        info = isFirst ? primitiveImageInfo : info.or(primitiveImageInfo);
        isFirst = false;
      }
    }

    this.imageInfo = info;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    buildBody(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", name);
    st.add("pack", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".decoder", modelName));
    st.add("ext", DecoderGroup.class.getSimpleName());
    st.add("imps", DecoderGroup.class.getName());
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("group_constructor");
    final BitVector opcMask = imageInfo.getOpcMask();

    stConstructor.add("name", name);
    stConstructor.add("size", imageInfo.getMaxImageSize());
    stConstructor.add("is_fixed", Boolean.toString(imageInfo.isImageSizeFixed()));
    stConstructor.add("opc_mask", opcMask != null ? "\"" + opcMask.toBinString() + "\"" : "null");
    stConstructor.add("items", items);

    st.add("members", stConstructor);
  }
}
