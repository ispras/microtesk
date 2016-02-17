/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public class DataSection {
  private final List<DataDirective> directives;
  private final boolean global;
  private final boolean separateFile;

  protected DataSection(
      final List<DataDirective> directives,
      final boolean global,
      final boolean separateFile) {
    InvariantChecks.checkNotNull(directives);

    this.directives = Collections.unmodifiableList(directives);
    this.global = global;
    this.separateFile = separateFile;
  }

  protected DataSection(final DataSection other) {
    InvariantChecks.checkNotNull(other);
    this.directives = other.directives;
    this.global = other.global;
    this.separateFile = other.separateFile;
  }

  public List<DataDirective> getDirectives() {
    return directives;
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }
}
