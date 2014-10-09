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

package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LazyData;
import ru.ispras.microtesk.test.template.LazyValue;

public final class PreparatorBuilder {
  private final String targetName;
  private final LazyData data;
  private final List<Call> calls;

  PreparatorBuilder(String targetName) {
    if (null == targetName) {
      throw new NullPointerException();
    }

    this.targetName = targetName;
    this.data = new LazyData();
    this.calls = new ArrayList<Call>();
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
    if (null == call) {
      throw new NullPointerException();
    }
    calls.add(call);
  }

  public Preparator build() {
    return new Preparator(targetName, data, calls);
  }
}
