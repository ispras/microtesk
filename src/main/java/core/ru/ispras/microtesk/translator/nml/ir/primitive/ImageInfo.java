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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;

public final class ImageInfo {
  public static final class Token {
    private final int bitSize;
    private final Node expr;

    private Token(final int bitSize, final Node expr) {
      this.bitSize = bitSize;
      this.expr = expr;
    }

    public int getBitSize() {
      return bitSize;
    }

    public Node getExpr() {
      return expr;
    }

    @Override
    public String toString() {
      return String.format("%d:%s", bitSize, expr);
    }
  }

  public static Token newOpc(final int bitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);
    return new Token(bitSize, null);
  }

  public static Token newField(final int bitSize, final Node expr) {
    InvariantChecks.checkNotNull(expr);
    return new Token(bitSize, expr);
  }

  private final int maxImageSize;
  private final boolean imageSizeFixed;
  private BitVector opc;
  private BitVector opcMask;
  private List<Token> tokens;

  public ImageInfo(final int maxImageSize, final boolean imageSizeFixed) {
    InvariantChecks.checkGreaterOrEqZero(maxImageSize);

    this.maxImageSize = maxImageSize;
    this.imageSizeFixed = imageSizeFixed;
    this.opc = null;
    this.opcMask = null;
    this.tokens = Collections.emptyList();

  }

  public int getMaxImageSize() {
    return maxImageSize;
  }

  public boolean isImageSizeFixed() {
    return imageSizeFixed;
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

  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(final List<Token> tokens) {
    InvariantChecks.checkNotNull(tokens);
    this.tokens = tokens;
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

  @Override
  public String toString() {
    return String.format(
        "ImageInfo [maxImageSize=%s, imageSizeFixed=%s, opc=%s, opcMask=%s, tokens=%s]",
        maxImageSize,
        imageSizeFixed,
        opc,
        opcMask,
        tokens
        );
  }
}
