/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import org.stringtemplate.v4.ST;

import java.util.Set;

final class Importer {
  private final ST st;
  private final Set<String> imports;

  public Importer(final ST st, final Set<String> imports) {
    this.imports = imports;
    this.st = st;
  }

  public void addImport(final String name) {
    if (!imports.contains(name)) {
      st.add("imps", name);
      imports.add(name);
    }
  }
}
