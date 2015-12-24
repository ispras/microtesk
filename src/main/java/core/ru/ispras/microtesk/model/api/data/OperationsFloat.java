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

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.fp.FloatX;

public final class OperationsFloat implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new OperationsFloat();
    }
    return instance;
  }

  private OperationsFloat() {}

  @Override
  public Data negate(final Data arg) {
    final FloatX result = arg.floatXValue().neg();
    return new Data(arg.getType(), result.getData());
  }

  @Override
  public Data add(final Data lhs, final Data rhs) {
    final FloatX result = lhs.floatXValue().add(rhs.floatXValue());
    return new Data(lhs.getType(), result.getData());
  }

  @Override
  public Data subtract(final Data lhs, final Data rhs) {
    final FloatX result = lhs.floatXValue().sub(rhs.floatXValue());
    return new Data(lhs.getType(), result.getData());
  }

  @Override
  public Data multiply(final Data lhs, final Data rhs) {
    final FloatX result = lhs.floatXValue().mul(rhs.floatXValue());
    return new Data(lhs.getType(), result.getData());
  }

  @Override
  public Data divide(final Data lhs, final Data rhs) {
    final FloatX result = lhs.floatXValue().div(rhs.floatXValue());
    return new Data(lhs.getType(), result.getData());
  }

  @Override
  public Data mod(Data lhs, Data rhs) {
    final FloatX result = lhs.floatXValue().mod(rhs.floatXValue());
    return new Data(lhs.getType(), result.getData());
  }

  @Override
  public Data pow(final Data lhs, final Data rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data not(final Data arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data and(final Data lhs, final Data rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data or(final Data lhs, final Data rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data xor(final Data lhs, final Data rhs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data shiftLeft(final Data value, final Data amount) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data shiftRight(final Data value, final Data amount) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data rotateLeft(Data value, Data amount) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Data rotateRight(final Data value, final Data amount) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compare(final Data lhs, final Data rhs) {
    return lhs.floatXValue().compareTo(rhs.floatXValue());
  }

  @Override
  public String toString(final Data arg) {
    return arg.floatXValue().toString();
  }

  @Override
  public String toHexString(final Data arg) {
    return arg.floatXValue().toHexString();
  }
}
