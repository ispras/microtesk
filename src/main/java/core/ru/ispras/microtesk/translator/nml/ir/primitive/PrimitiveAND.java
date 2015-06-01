/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrimitiveAND extends Primitive {
  private final Expr retExpr;
  private final Map<String, Primitive> args;
  private final Map<String, Attribute> attrs;
  private final List<Shortcut> shortcuts;

  PrimitiveAND(
      final String name,
      final Kind kind,
      final Expr retExpr,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) {
    super(name, kind, false, getReturnType(retExpr), null == attrs ? null : attrs.keySet());

    this.retExpr = retExpr;
    this.args = args;
    this.attrs = Collections.unmodifiableMap(attrs);
    this.shortcuts = new ArrayList<Shortcut>();

    for (Map.Entry<String, Primitive> e : args.entrySet()) {
      final Primitive target = e.getValue();
      final String referenceName = e.getKey();

      target.addParentReference(this, referenceName);
    }
  }

  private PrimitiveAND(PrimitiveAND source) {
    super(source);

    this.retExpr = source.retExpr;
    this.args = new LinkedHashMap<String, Primitive>(source.args);
    this.attrs = source.attrs;
    this.shortcuts = source.shortcuts;
  }

  void addShortcut(Shortcut shortcut) {
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

  private static Type getReturnType(Expr retExpr) {
    return (null != retExpr) ? retExpr.getValueInfo().getModelType() : null;
  }
}
