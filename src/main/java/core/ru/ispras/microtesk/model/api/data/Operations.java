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

package ru.ispras.microtesk.model.api.data;

public interface Operations {
  Data negate(final Data arg);
  Data add(final Data lhs, final Data rhs);
  Data subtract(final Data lhs, final Data rhs);

  Data multiply(final Data lhs, final Data rhs);
  Data divide(final Data lhs, final Data rhs);
  Data mod(final Data lhs, final Data rhs);
  Data pow(final Data lhs, final Data rhs);

  Data not(final Data arg);
  Data and(final Data lhs, final Data rhs);
  Data or(final Data lhs, final Data rhs);
  Data xor(final Data lhs, final Data rhs);

  Data shiftLeft(final Data value, final Data amount);
  Data shiftRight(final Data value, final Data amount);
  Data rotateLeft(final Data value, final Data amount);
  Data rotateRight(final Data value, final Data amount);

  int compare(final Data lhs, final Data rhs);

  String toString(final Data arg);
  String toHexString(final Data arg);
}
