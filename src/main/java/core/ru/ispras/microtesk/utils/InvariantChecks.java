/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

public final class InvariantChecks {

  private InvariantChecks() {}

  public static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }

  public static void checkNotNull(Object o, String name) {
    if (null == o) {
      throw new NullPointerException(
          String.format("%s must not be equal null", name));
    }
  }

  public static void checkGreaterThanZero(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException(
          String.format("%d must be > 0", n));      
    }
  }

  public static void checkGreaterOrEqZero(int n) {
    if (n < 0) {
      throw new IllegalArgumentException(
          String.format("%d must be >= 0", n));      
    }
  }

  public static void checkBounds(int index, int length) {
    if (!(0 <= index && index < length)) {
      throw new IndexOutOfBoundsException(String.format(
          "%d must be within range [0, %d)", index, length));
    }
  }
}
