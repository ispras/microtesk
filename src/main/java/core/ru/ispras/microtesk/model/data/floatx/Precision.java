/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.data.floatx;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.softfloat.JSoftFloat;

/**
 * {@link Precision} describes supported precisions of floating-point numbers.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public enum Precision {
  FLOAT16 (10, 5) {
    @Override public Operations getOperations() {
      return Float16Operations.get();
    }
  },

  FLOAT32 (23, 8) {
    @Override public Operations getOperations() {
      return Float32Operations.get();
    }
  },

  FLOAT64 (52, 11) {
    @Override public Operations getOperations() {
      return Float64Operations.get();
    }
  },

  FLOAT80 (64, 15) {
    @Override public Operations getOperations() {
      return Float80Operations.get();
    }
  },

  FLOAT128 (112, 15) {
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
    return fractionSize + exponentSize + 1; // plus implicit sign bit
  }

  public final String getText() {
    return String.format("float(%d, %d)", fractionSize, exponentSize);
  }

  public abstract Operations getOperations();

  public static Precision find(final int fractionSize, final int exponentSize) {
    for (final Precision precision : Precision.values()) {
      if (precision.getFractionSize() == fractionSize &&
          precision.getExponentSize() == exponentSize) {
        return precision;
      }
    }

    return null;
  }

  static int getRoundingMode() {
    return JSoftFloat.getFloatRoundingMode();
  }

  static void setRoundingMode(final int value) {
    JSoftFloat.setFloatRoundingMode(value);
  }

  static int getExceptionFlags() {
    return JSoftFloat.getFloatExceptionFlags();
  }

  static void setExceptionFlags(final int value) {
    JSoftFloat.setFloatExceptionFlags(value);
  }
}
