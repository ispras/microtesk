/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.expression.NodeVariable;

final class SsaScopeEmpty implements SsaScope {
  private static SsaScope instance = null;

  public static SsaScope get() {
    if (null == instance) {
      instance = new SsaScopeEmpty();
    }
    return instance;
  }

  private SsaScopeEmpty() {}

  @Override
  public boolean contains(final String name) {
    return false;
  }

  @Override
  public NodeVariable create(final String name, final Data data) {
    throw new UnsupportedOperationException();
  }

  @Override
  public NodeVariable fetch(final String name) {
    throw new IllegalArgumentException();
  }

  @Override
  public NodeVariable update(final String name) {
    throw new IllegalArgumentException();
  }
}
