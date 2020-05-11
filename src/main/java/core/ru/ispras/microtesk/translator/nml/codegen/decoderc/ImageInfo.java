/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.decoderc;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import java.util.Collections;
import java.util.List;

final class ImageInfo {
  private final int maxImageSize;
  private final boolean imageSizeFixed;

  private BitVector opc;
  private BitVector opcMask;
  private List<Pair<Node, ImageInfo>> fields;

  public ImageInfo(final ImageInfo other) {
    InvariantChecks.checkNotNull(other);

    this.maxImageSize = other.maxImageSize;
    this.imageSizeFixed = other.imageSizeFixed;
    this.opc = other.opc;
    this.opcMask = other.opcMask;
    this.fields = other.fields;
  }

  public ImageInfo(final int maxImageSize, final boolean imageSizeFixed) {
    InvariantChecks.checkGreaterOrEqZero(maxImageSize);

    this.maxImageSize = maxImageSize;
    this.imageSizeFixed = imageSizeFixed;
    this.opc = null;
    this.opcMask = null;
    this.fields = Collections.emptyList();
  }

  public int getMaxImageSize() {
    return maxImageSize;
  }

  public boolean isImageSizeFixed() {
    return imageSizeFixed;
  }

  public ImageInfo or(final ImageInfo other) {
    InvariantChecks.checkNotNull(other);
    return new ImageInfo(
        Math.max(this.maxImageSize, other.maxImageSize),
        this.maxImageSize == other.maxImageSize && this.imageSizeFixed && other.imageSizeFixed
        );
  }

  public ImageInfo and(final ImageInfo other) {
    InvariantChecks.checkNotNull(other);
    return new ImageInfo(
        this.maxImageSize + other.maxImageSize,
        this.imageSizeFixed && other.imageSizeFixed
        );
  }

  public BitVector getOpc() {
    return opc;
  }

  public void setOpc(final BitVector value) {
    this.opc = value;
  }

  public BitVector getOpcMask() {
    return opcMask;
  }

  public void setOpcMask(final BitVector value) {
    this.opcMask = value;
  }

  public List<Pair<Node, ImageInfo>> getFields() {
    return fields;
  }

  public void setFields(final List<Pair<Node, ImageInfo>> fields) {
    InvariantChecks.checkNotNull(fields);
    this.fields = fields;
  }

  @Override
  public String toString() {
    return String.format(
        "ImageInfo [maxImageSize=%s, imageSizeFixed=%s, opc=%s, opcMask=%s, fields=%s]",
        maxImageSize,
        imageSizeFixed,
        opc,
        opcMask,
        fields
        );
  }
}
