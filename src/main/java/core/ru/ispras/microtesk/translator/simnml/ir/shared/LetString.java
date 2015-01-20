/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import static ru.ispras.microtesk.utils.InvariantChecks.checkNotNull;

public final class LetString {
  private final String name;
  private final String text;

  LetString(String name, String text) {
    checkNotNull(name);
    checkNotNull(text);

    this.name = name;
    this.text = text;
  }

  public String getName() {
    return name;
  }

  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return String.format("LetString [name=%s, text=%s]", name, text);
  }
}
