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

package ru.ispras.microtesk.translator.nml.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.nml.ir.shared.LetString;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class Ir {
  private final Map<String, LetConstant> consts;
  private final Map<String, LetString> strings;
  private final Map<String, LetLabel> labels;
  private final Map<String, Type> types;
  private final Map<String, MemoryExpr> memory;
  private final Map<String, Primitive> modes;
  private final Map<String, Primitive> ops;
  private List<Primitive> roots;

  public Ir() {
    this.consts  = new LinkedHashMap<>();
    this.strings = new LinkedHashMap<>();

    this.labels  = new LinkedHashMap<>();
    this.types   = new LinkedHashMap<>();
    this.memory  = new LinkedHashMap<>();

    this.modes   = new LinkedHashMap<>();
    this.ops     = new LinkedHashMap<>();

    this.roots = Collections.<Primitive>emptyList();
  }

  public void add(final String name, final LetConstant value) {
    checkNotNull(name);
    checkNotNull(value);
    consts.put(name, value);
  }

  public void add(final String name, final LetString value) {
    checkNotNull(name);
    checkNotNull(value);
    strings.put(name, value);
  }

  public void add(final String name, final LetLabel value) {
    checkNotNull(name);
    checkNotNull(value);
    labels.put(name, value);
  }

  public void add(final String name, final Type value) {
    checkNotNull(name);
    checkNotNull(value);
    types.put(name, value);
  }

  public void add(final String name, final MemoryExpr value) {
    checkNotNull(name);
    checkNotNull(value);
    memory.put(name, value);
  }

  public void add(final String name, final Primitive value) {
    checkNotNull(name);
    checkNotNull(value);

    if (Primitive.Kind.MODE == value.getKind()) {
      modes.put(name, value);
    }
    else if (Primitive.Kind.OP == value.getKind()) {
      ops.put(name, value);
    }
    else {
      throw new IllegalArgumentException(
        "Illegal primitive kind: " + value.getKind());
    }
  }

  public Map<String, LetConstant> getConstants() {
    return Collections.unmodifiableMap(consts);
  }

  public Map<String, LetString> getStrings() {
    return Collections.unmodifiableMap(strings);
  }

  public Map<String, LetLabel> getLabels() {
    return Collections.unmodifiableMap(labels);
  }

  public Map<String, Type> getTypes() {
    return Collections.unmodifiableMap(types);
  }

  public Map<String, MemoryExpr> getMemory() {
    return Collections.unmodifiableMap(memory);
  }

  public Map<String, Primitive> getModes() {
    return Collections.unmodifiableMap(modes);
  }

  public Map<String, Primitive> getOps() {
    return Collections.unmodifiableMap(ops);
  }

  public List<Primitive> getRoots() {
    return roots;
  }

  public void setRoots(final List<Primitive> value) {
    checkNotNull(value);

    if (!roots.isEmpty()) {
      throw new IllegalArgumentException("Root is already assigned.");
    }

    roots = Collections.unmodifiableList(roots);
  }
}
