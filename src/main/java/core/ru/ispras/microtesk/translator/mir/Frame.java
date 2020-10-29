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

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Frame {
  public final Map<String, List<Operand>> globals;
  public final List<Operand> locals = new java.util.ArrayList<>();

  public Frame() {
    this.globals = new java.util.HashMap<>();
  }

  public Frame(final Map<String, List<Operand>> globals) {
    this.globals = globals;
  }

  public Operand get(final String name, final int version) {
    final List<Operand> variants = getValues(name);
    if (variants.size() >= version) {
      return variants.get(version - 1);
    }
    return VoidTy.VALUE;
  }

  List<Operand> getValues(final String name) {
    return globals.getOrDefault(name, Collections.emptyList());
  }

  void set(final String name, final int version, final Operand value) {
    final List<Operand> values =
        globals.computeIfAbsent(name, k -> new java.util.ArrayList<>());
    final int ndiff = version - values.size();
    if (ndiff > 0) {
      values.addAll(Collections.nCopies(ndiff, VoidTy.VALUE));
    }
    values.set(version - 1, value);
  }
}
