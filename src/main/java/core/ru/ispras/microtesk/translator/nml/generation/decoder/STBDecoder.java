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
import ru.ispras.microtesk.decoder.Decoder;
import ru.ispras.microtesk.decoder.DecoderResult;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.ImageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

final class STBDecoder implements STBuilder {
  private final String name;
  private final String modelName;
  private final ImageInfo imageInfo;

  public STBDecoder(final String modelName, final PrimitiveAND item) {
    InvariantChecks.checkNotNull(modelName);
    InvariantChecks.checkNotNull(item);
    InvariantChecks.checkNotNull(item.getInfo().getImageInfo());

    this.name = DecoderGenerator.getDecoderName(item.getName());
    this.modelName = modelName;
    this.imageInfo = item.getInfo().getImageInfo();
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
    st.add("ext", Decoder.class.getSimpleName());
    st.add("imps", Decoder.class.getName());
    st.add("imps", DecoderResult.class.getName());
    st.add("imps", BitVector.class.getName());
    st.add("instance", "instance");
  }

  private void buildBody(final ST st, final STGroup group) {
    final ST stConstructor = group.getInstanceOf("decoder_constructor");

    stConstructor.add("name", name);
    stConstructor.add("size", imageInfo.getMaxImageSize());
    stConstructor.add("is_fixed", Boolean.toString(imageInfo.isImageSizeFixed()));

    final BitVector opc = imageInfo.getOpc();
    final BitVector opcMask = imageInfo.getOpcMask();

    stConstructor.add("opc", null != opc ? "\"" + opc.toBinString() + "\"": "null");
    stConstructor.add("opc_mask", null != opcMask ? "\"" + opcMask.toBinString() + "\"" : "null");

    st.add("members", stConstructor);
  }
}
