/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.util.List;

public final class BasicBlock {
  public final List<Instruction> insns = new java.util.ArrayList<>();
  public final List<Origin> origins = new java.util.ArrayList<>();

  public BasicBlock() {
    this.origins.add(new Origin(0, 0));
  }

  public static BasicBlock copyOf(final BasicBlock bb) {
    final BasicBlock copy = new BasicBlock();

    copy.insns.addAll(bb.insns);
    copy.origins.clear();
    for (final Origin org : bb.origins) {
      copy.origins.add(new Origin(org.range, org.value));
    }
    return copy;
  }

  public int getOrigin(final int index) {
    for (int i = origins.size() - 1; i >= 0; --i) {
      final Origin org = origins.get(i);
      if (index >= org.range) {
        return org.value;
      }
    }
    throw new IllegalStateException();
  }

  public static final class Origin {
    public int range;
    public int value;

    public Origin(int range, int value) {
      this.range = range;
      this.value = value;
    }
  }
}
