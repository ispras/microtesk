/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Primitive {
  public static enum Kind {
    OP("op"),
    MODE("mode");

    private final String text;

    private Kind(final String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  };

  Primitive newCopy();
  Kind getKind();
  String getName();
  String getTypeName();

  boolean isRoot();
  Map<String, Argument> getArguments();
  String getContextName();
  boolean hasSituation();
  Situation getSituation();
  String getSignature();

  boolean canThrowException();
  boolean isBranch();
  boolean isConditionalBranch();

  boolean isLoad();
  boolean isStore();
  int getBlockSize();
}

final class ConcretePrimitive implements Primitive {
  private final Kind kind;
  private final String name;
  private final String typeName;
  private final boolean isRoot;
  private final Map<String, Argument> args;
  private final String contextName;
  private final Situation situation;

  private final boolean branch;
  private final boolean conditionalBranch;
  private final boolean exception;

  private final boolean load;
  private final boolean store;
  private final int blockSize;

  protected ConcretePrimitive(
      final Kind kind,
      final String name,
      final String typeName,
      final boolean isRoot,
      final Map<String, Argument> args,
      final String contextName,
      final Situation situation,
      final boolean branch,
      final boolean conditionalBranch,
      final boolean exception,
      final boolean load,
      final boolean store,
      final int blockSize) {
    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(typeName);
    checkNotNull(args);

    this.kind = kind;
    this.name = name;
    this.typeName = typeName;
    this.isRoot = isRoot;
    this.args = args;
    this.contextName = contextName;
    this.situation = situation;

    this.branch = branch;
    this.conditionalBranch = conditionalBranch;
    this.exception = exception;

    this.load = load;
    this.store = store;
    this.blockSize = blockSize;
  }

  private ConcretePrimitive(final ConcretePrimitive other) {
    this.kind = other.kind;
    this.name = other.name;
    this.typeName = other.typeName;
    this.isRoot = other.isRoot;
    this.args = copyArguments(other.args);
    this.contextName = other.contextName;
    this.situation = other.situation;

    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;
    this.exception = other.exception;

    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;
  }

  public static Map<String, Argument> copyArguments(
      final Map<String, Argument> args) {

    if (args.isEmpty()) {
      return Collections.emptyMap();
    }

    final Map<String, Argument> result = new LinkedHashMap<>(args.size()); 
    for (final Map.Entry<String, Argument> entry : args.entrySet()) {
      result.put(entry.getKey(), new Argument(entry.getValue()));
    }

    return result;
  }

  @Override
  public Primitive newCopy() {
    return new ConcretePrimitive(this);
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isRoot() {
    return isRoot;
  }

  public Map<String, Argument> getArguments() {
    return args;
  }

  public String getContextName() {
    return contextName;
  }

  public boolean hasSituation() {
    return null != situation;
  }

  public Situation getSituation() {
    return situation;
  }

  public String getSignature() {
    final StringBuilder sb = new StringBuilder();

    for (Argument arg : args.values()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(arg.getName() + ": " + arg.getTypeName());
    }

    final String signature = String.format("%s %s(%s)", kind.getText(), name, sb);
    if (kind == Kind.MODE) {
      return signature;
    }

    // The rest attributes make sense only for OPs.
    return signature + String.format(
      ":[context=%s, type=%s, root=%b]", contextName, typeName, isRoot);
  }

  @Override
  public boolean canThrowException() {
    return exception;
  }

  @Override
  public boolean isBranch() {
    return branch;
  }

  @Override
  public boolean isConditionalBranch() {
    return conditionalBranch;
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

  @Override
  public String toString() {
    return null == situation ?
       getSignature() : String.format("%s, situation=%s", getSignature(), situation);
  }
}

final class LazyPrimitive implements Primitive {
  private Primitive source;
  private final Kind kind;
  private final String name;
  private final String typeName;

  protected LazyPrimitive(
      final Kind kind,
      final String name,
      final String typeName) {
    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(typeName);

    this.source = null;
    this.kind = kind;
    this.name = name;
    this.typeName = typeName;
  }

  private LazyPrimitive(final LazyPrimitive other) {
    this.source = null != other.source ? other.source.newCopy() : null;
    this.kind = other.kind;
    this.name = other.name;
    this.typeName = other.typeName;
  }

  public void setSource(final Primitive source) {
    checkCompatible(source);
    this.source = source;
  }

  private void checkCompatible(final Primitive p) {
    if (null == p) { // null can be used to reset the field.
      return;
    }

    if (this == source) {
      throw new IllegalArgumentException("Link to self!");
    }

    if (kind != p.getKind()) {
      throw new IllegalArgumentException(String.format(
        "Incompatible kind: %s (%s is expected).", p.getKind(), kind));
    }

    if (!name.equals(p.getName())) {
      throw new IllegalArgumentException(String.format(
        "Incompatible name: %s (%s is expected).", p.getName(), name));
    }

    if (!typeName.equals(p.getTypeName())) {
      throw new IllegalArgumentException(String.format(
        "Incompatible type: %s (%s is expected).", p.getTypeName(), typeName));
    }
  }

  @Override
  public Primitive newCopy() {
    return new LazyPrimitive(this);
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public boolean isRoot() {
    checkSourceAssigned();
    return source.isRoot();
  }

  @Override
  public Map<String, Argument> getArguments() {
    checkSourceAssigned();
    return source.getArguments();
  }

  @Override
  public String getContextName() {
    checkSourceAssigned();
    return source.getContextName();
  }

  @Override
  public boolean hasSituation() {
    checkSourceAssigned();
    return source.hasSituation();
  }

  @Override
  public Situation getSituation() {
    checkSourceAssigned();
    return source.getSituation();
  }

  @Override
  public String getSignature() {
    checkSourceAssigned();
    return source.getSignature();
  }

  @Override
  public boolean canThrowException() {
    checkSourceAssigned();
    return source.canThrowException();
  }

  @Override
  public boolean isBranch() {
    checkSourceAssigned();
    return source.isBranch();
  }

  @Override
  public boolean isConditionalBranch() {
    checkSourceAssigned();
    return source.isConditionalBranch();
  }

  @Override
  public boolean isLoad() {
    checkSourceAssigned();
    return source.isLoad();
  }

  @Override
  public boolean isStore() {
    checkSourceAssigned();
    return source.isStore();
  }

  @Override
  public int getBlockSize() {
    checkSourceAssigned();
    return source.getBlockSize();
  }

  @Override
  public String toString() {
    if (null != source) {
      return source.toString();
    }

    return String.format(
      "lazy %s %s [type = %s]", kind.getText(), name, typeName); 
  }

  private void checkSourceAssigned() {
    if (null == source) {
      throw new IllegalStateException(String.format(
        "Source for %s is not assigned.", name));
    }
  }
}
