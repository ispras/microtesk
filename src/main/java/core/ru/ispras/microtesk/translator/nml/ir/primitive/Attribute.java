/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Attribute {
  public static final String SYNTAX_NAME = "syntax";
  public static final String IMAGE_NAME  = "image";
  public static final String ACTION_NAME = "action";
  public static final String INIT_NAME   = "init";

  public static final Set<String> STANDARD_NAMES = createStandardNames();

  public static enum Kind {
    ACTION,
    EXPRESSION
  }

  private final String name;
  private final Kind kind;
  private final List<Statement> stmts;

  Attribute(final String name, final Kind kind, final List<Statement> stmts) {
    checkNotNull(name);
    checkNotNull(kind);
    checkNotNull(stmts);

    this.name = name;
    this.kind = kind;
    this.stmts = Collections.unmodifiableList(stmts);
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return kind;
  }

  public List<Statement> getStatements() {
    return stmts;
  }

  private static Set<String> createStandardNames() {
    final Set<String> result = new HashSet<>();

    result.add(SYNTAX_NAME);
    result.add(IMAGE_NAME);
    result.add(ACTION_NAME);

    return Collections.unmodifiableSet(result);
  }
}
