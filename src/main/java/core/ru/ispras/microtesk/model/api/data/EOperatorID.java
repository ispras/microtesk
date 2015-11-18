/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

public enum EOperatorID {
  // Arithmetical operators
  PLUS, MINUS, UNARY_PLUS, UNARY_MINUS, MUL, DIV, MOD, POW,

  // Comparison operators
  GREATER, LESS, GREATER_EQ, LESS_EQ, EQ, NOT_EQ,

  // Bitwise operators
  BIT_AND, BIT_OR, BIT_XOR, BIT_NOT, L_SHIFT, R_SHIFT, L_ROTATE, R_ROTATE,

  // Logical operators
  AND, OR, NOT,

  // Floating-point operators
  SQRT,
  IS_NAN,
  IS_SIGNALING_NAN
}
