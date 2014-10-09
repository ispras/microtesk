/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PreparatorBuilder.java, Oct 8, 2014 7:34:48 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.preparator;

import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.LazyValue;

public final class PreparatorBuilder {
  private final String target;
  private final LazyData data;

  public PreparatorBuilder(String target) {
    if (null == target) {
      throw new NullPointerException();
    }

    this.data = new LazyData();
    this.target = target;
  }

  public LazyValue newValue() {
    return new LazyValue(data);
  }

  public LazyValue newValue(int start, int end) {
    return new LazyValue(data, start, end);
  }

  public Object getTarget() {
    return null;
  }

  public void addCall(Call call) {

  }

  public Preparator build() {
    return new Preparator(target, data);
  }
}
