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

final class Float32Operations implements Operations {
  private static Operations instance = null;

  public static Operations get() {
    if (null == instance) {
      instance = new Float32Operations();
    }
    return instance;
  }

  private Float32Operations() {}

  @Override
  public FloatX neg(final FloatX arg) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FloatX add(final FloatX lhs, final FloatX rhs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FloatX sub(final FloatX lhs, final FloatX rhs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FloatX mul(final FloatX lhs, final FloatX rhs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FloatX div(final FloatX lhs, final FloatX rhs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FloatX sqrt(final FloatX arg) {
    // TODO Auto-generated method stub
    return null;
  }
}
