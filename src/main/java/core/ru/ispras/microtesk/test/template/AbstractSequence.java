/*
 * Copyright 2014-2017 ISP RAS (http://www.ispras.ru)
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

public final class AbstractSequence {
  public static final AbstractSequence EMPTY = new AbstractSequence();

  private final List<AbstractCall> sequence;

  // FIXME: remove.
  private Object userData;

  public AbstractSequence() {
    this.sequence = new ArrayList<>();
  }

  public AbstractSequence(final List<AbstractCall> sequence) {
    this.sequence = sequence;
  }

  public boolean isEmpty() {
    return sequence.isEmpty();
  }

  public int size() {
    return sequence.size();
  }

  public List<AbstractCall> getSequence() {
    return sequence;
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(final Object userData) {
    this.userData = userData;
  }

  @Override
  public String toString() {
    return sequence.toString();
  }
}
