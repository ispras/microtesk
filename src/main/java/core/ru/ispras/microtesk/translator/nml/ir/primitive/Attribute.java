/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The {@code Attribute} class describes an attribute of an nML primitive (OP or MODE).
 * It stores a collection of statements.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Attribute {
  public enum Kind {
    ACTION,
    EXPRESSION
  }

  public static final String SYNTAX_NAME = "syntax";
  public static final String IMAGE_NAME  = "image";
  public static final String ACTION_NAME = "action";
  public static final String INIT_NAME   = "init";
  public static final String DECODE_NAME = "decode";

  private static final Set<String> STANDARD_NAMES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          SYNTAX_NAME,
          IMAGE_NAME,
          ACTION_NAME,
          INIT_NAME,
          DECODE_NAME
          )));

  private final String name;
  private final Kind kind;
  private final boolean isStandard;
  private final List<Statement> stmts;

  public Attribute(
      final String name,
      final Kind kind,
      final List<Statement> stmts) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(stmts);

    this.name = name;
    this.kind = kind;
    this.isStandard = isStandard(name);
    this.stmts = new LinkedList<>(stmts);
  }

  public static boolean isStandard(final String name) {
    return STANDARD_NAMES.contains(name);
  }

  public boolean isStandard() {
    return isStandard;
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return kind;
  }

  public List<Statement> getStatements() {
    return Collections.unmodifiableList(stmts);
  }

  public void insertStatement(final Statement stmt) {
    InvariantChecks.checkNotNull(stmt);
    stmts.add(0, stmt);
  }
}
