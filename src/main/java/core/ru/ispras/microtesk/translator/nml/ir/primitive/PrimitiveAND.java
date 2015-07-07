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

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrimitiveAND extends Primitive {
  private final Expr retExpr;
  private final Map<String, Primitive> args;
  private Map<String, ArgumentMode> argsUsage;
  private final Map<String, Attribute> attrs;
  private final List<Shortcut> shortcuts;

  private final boolean exception;
  private boolean branch;
  private boolean conditionalBranch;

  private final boolean memoryReference;
  private final boolean load;
  private final boolean store;
  private final int blockSize;

  protected PrimitiveAND(
      final String name,
      final Kind kind,
      final Expr retExpr,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs,
      final boolean exception,
      final boolean memoryReference,
      final boolean load,
      final boolean store,
      final int blockSize) {
    super(
        name,
        kind,
        false,
        getReturnType(retExpr), null == attrs ? null : attrs.keySet()
    );

    this.retExpr = retExpr;
    this.args = args;
    this.argsUsage = Collections.emptyMap();
    this.attrs = Collections.unmodifiableMap(attrs);
    this.shortcuts = new ArrayList<>();

    for (final Map.Entry<String, Primitive> e : args.entrySet()) {
      final Primitive target = e.getValue();
      final String referenceName = e.getKey();

      target.addParentReference(this, referenceName);
    }

    this.exception = exception;
    this.memoryReference = memoryReference;
    this.load = false; // TODO
    this.store = false; // TODO
    this.blockSize = 0; // TODO
  }

  private PrimitiveAND(final PrimitiveAND other) {
    super(other);

    this.retExpr = other.retExpr;
    this.args = new LinkedHashMap<>(other.args);
    this.argsUsage = other.argsUsage;
    this.attrs = other.attrs;
    this.shortcuts = other.shortcuts;

    this.exception = other.exception;
    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;

    this.memoryReference = other.memoryReference;
    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;
  }

  void addShortcut(final Shortcut shortcut) {
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

  private static Type getReturnType(final Expr retExpr) {
    return (null != retExpr) ? retExpr.getValueInfo().getModelType() : null;
  }

  public ArgumentMode getArgUsage(final String name) {
    final ArgumentMode result = argsUsage.get(name);
    return result != null ? result : ArgumentMode.NA;
  }

  void setArgsUsage(final Map<String, ArgumentMode> argsUsage) {
    this.argsUsage = argsUsage;
  }

  public boolean canThrowException() {
    return exception;
  }

  public boolean isBranch() {
    return branch;
  }

  public void setBranch(final boolean value) {
    this.branch = value;
    if (!value) {
      conditionalBranch = false;
    }
  }

  public boolean isConditionalBranch() {
    return conditionalBranch;
  }

  public void setConditionalBranch(final boolean value) {
    this.conditionalBranch = value;
    if (value) {
      branch = true;
    }
  }

  public boolean isMemoryReference() {
    return memoryReference;
  }

  public boolean isLoad() {
    return load;
  }

  public boolean isStore() {
    return store;
  }

  public int getBlockSize() {
    return blockSize;
  }
}
