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

package ru.ispras.microtesk.mmu.translator.ir;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class ExternalSource {
  public enum Kind {
    MEMORY, // reg or mem
    MODE    // addressing mode
  }

  private final Kind kind;
  private final String name;
  private final List<BigInteger> args;

  public ExternalSource(
      final Kind kind,
      final String name,
      final List<BigInteger> args) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(args);

    this.kind = kind;
    this.name = name;
    this.args = Collections.unmodifiableList(args);
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public List<BigInteger> getArgs() {
    return args;
  }

  @Override
  public String toString() {
    return String.format(
        "ExternalSource [kind=%s, name=%s, args=%s]", kind, name, args);
  }
}
