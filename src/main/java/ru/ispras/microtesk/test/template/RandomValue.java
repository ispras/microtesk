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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.randomizer.Randomizer;

public final class RandomValue {
  private final int min;
  private final int max;

  private int value;
  private boolean isValueGenerated;

  RandomValue(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException(String.format("min (%d) must be <= max (%d)!", min, max));
    }

    this.min = min;
    this.max = max;
    this.value = 0;
    this.isValueGenerated = false;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }

  public int getValue() {
    if (!isValueGenerated) {
      value = Randomizer.get().nextIntRange(min, max);
      isValueGenerated = true;
    }
    return value;
  }
}
