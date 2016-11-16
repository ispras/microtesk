/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrimitiveAND extends Primitive {
  private final Expr retExpr;
  private final Map<String, Primitive> args;
  private final Map<String, Attribute> attrs;
  private final List<Shortcut> shortcuts;

  protected PrimitiveAND(
      final String name,
      final Kind kind,
      final boolean pseudo,
      final Expr retExpr,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) {
    super(
        name,
        kind,
        pseudo,
        false,
        getReturnType(retExpr),
        getAttrNames(attrs)
    );

    this.retExpr = retExpr;
    this.args = args;
    this.attrs = Collections.unmodifiableMap(attrs);
    this.shortcuts = new ArrayList<>();
  }

  private PrimitiveAND(final PrimitiveAND other) {
    super(other);

    this.retExpr = other.retExpr;
    this.args = new LinkedHashMap<>(other.args);
    this.attrs = other.attrs;
    this.shortcuts = other.shortcuts;
  }

  private static Type getReturnType(final Expr retExpr) {
    return null != retExpr ? retExpr.getNodeInfo().getType() : null;
  }

  private static Set<String> getAttrNames(final Map<String, Attribute> attrs) {
    if (null == attrs) {
      return null;
    }

    final Set<String> result = new LinkedHashSet<>();
    for (final Attribute attribute : attrs.values()) {
      if (attribute.isStandard()) {
        result.add(attribute.getName());
      }
    }

    return result;
  }

  public void addShortcut(final Shortcut shortcut) {
    shortcuts.add(shortcut);
  }

  public PrimitiveAND makeCopy() {
    return new PrimitiveAND(this);
  }

  public Map<String, Primitive> getArguments() {
    return args;
  }

  public Map<String, Attribute> getAttributes() {
    return attrs;
  }

  public List<Shortcut> getShortcuts() {
    return Collections.unmodifiableList(shortcuts);
  }

  public Expr getReturnExpr() {
    return retExpr;
  }
}
