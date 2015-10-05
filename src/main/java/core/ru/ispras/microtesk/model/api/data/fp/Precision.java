/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data.fp;

import ru.ispras.fortress.util.InvariantChecks;

enum Precision {
  FLOAT32 (24, 8) {
    @Override public Operations getOperations() {
      return Float32Operations.get();
    }
  },

  FLOAT64 (53, 11) {
    @Override public Operations getOperations() {
      return Float64Operations.get();
    }
  },

  FLOAT80 (64, 16) {
    @Override public Operations getOperations() {
      return Float80Operations.get();
    }
  },

  FLOAT128 (113, 15) {
    @Override public Operations getOperations() {
      return Float128Operations.get();
    }
  };

  private final int fractionSize;
  private final int exponentSize;

  private Precision(final int fractionSize, final int exponentSize) {
    InvariantChecks.checkGreaterThanZero(fractionSize);
    InvariantChecks.checkGreaterThanZero(exponentSize);

    this.fractionSize = fractionSize;
    this.exponentSize = exponentSize;
  }

  public final int getFractionSize() {
    return fractionSize;
  }

  public final int getExponentSize() {
    return exponentSize;
  }

  public final int getSize() {
    return fractionSize + exponentSize;
  }

  public abstract Operations getOperations();

  public static Precision get(final int fractionSize, final int exponentSize) {
    for (final Precision precision : values()) {
      if (precision.fractionSize == fractionSize &&
          precision.exponentSize == exponentSize) {
        return precision;
      }
    }

    throw new IllegalStateException(String.format(
        "Unsupported floating-point precision: %d bits (fraction=%d, exponent=%d)",
        fractionSize + exponentSize, fractionSize, exponentSize)
        );
  }
}
