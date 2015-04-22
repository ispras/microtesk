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

package ru.ispras.microtesk.translator.nml.coverage;

final class Pair<T, U> {
  final T first;
  final U second;

  public Pair(T first, U second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    if (object instanceof Pair) {
      final Pair<?, ?> rhs = (Pair<?, ?>) object;
      return this.first != null &&
             rhs.first != null &&
             this.first.equals(rhs.first) &&
             this.second != null &&
             rhs.second != null &&
             this.second.equals(rhs.second);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * first.hashCode() + second.hashCode();
  }

  @Override
  public String toString() {
    return String.format("<%s, %s>", first, second);
  }
}
