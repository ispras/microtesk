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

public final class ExternalVariable {
  public enum SourceKind {
    MEMORY, // reg or mem
    MODE    // addressing mode
  }

  private final String name;
  private final Type type;
  private final SourceKind sourceKind;
  private final String sourceName;
  private final List<BigInteger> sourceArgs;

  public ExternalVariable(
      final String name,
      final Type type,
      final SourceKind sourceKind,
      final String sourceName,
      final List<BigInteger> sourceArgs) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(sourceKind);
    InvariantChecks.checkNotNull(sourceName);
    InvariantChecks.checkNotNull(sourceArgs);

    this.name = name;
    this.type = type;
    this.sourceKind = sourceKind;
    this.sourceName = sourceName;
    this.sourceArgs = Collections.unmodifiableList(sourceArgs);
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public SourceKind getSourceKind() {
    return sourceKind;
  }

  public String getSourceName() {
    return sourceName;
  }

  public List<BigInteger> getSourceArgs() {
    return sourceArgs;
  }
}
