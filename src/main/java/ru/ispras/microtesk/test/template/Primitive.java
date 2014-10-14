/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

import java.util.Map;

public interface Primitive
{
  public static enum Kind {
    OP("op"),
    MODE("mode");

    private final String text;

    private Kind(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  };

  public Kind getKind();
  public String getName();
  public String getTypeName();
  public boolean isRoot();
  public Map<String, Argument> getArguments();
  public String getContextName();
  public boolean hasSituation();
  public Situation getSituation();
  public String getSignature();
}

final class ConcretePrimitive implements Primitive {
  private final Kind kind;
  private final String name;
  private final String typeName;
  private final boolean isRoot;
  private final Map<String, Argument> args;
  private final String contextName;
  private final Situation situation;

  ConcretePrimitive(Kind kind, String name, String typeName, boolean isRoot, Map<String, Argument> args,
      String contextName, Situation situation) {
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
  public String toString() {
    return null == situation ?
       getSignature() : String.format("%s, situation=%s", getSignature(), situation);
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}

final class LazyPrimitive implements Primitive {
  private Primitive source;
  private final Kind kind;
  private final String name;
  private final String typeName;

  LazyPrimitive(Kind kind, String name, String typeName)
  {
    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(typeName);

    this.source = null;
    this.kind = kind;
    this.name = name;
    this.typeName = typeName;
  }

  public void setSource(Primitive source) {
    checkCompatible(source);
    this.source = source;
  }

  private void checkCompatible(Primitive p) {
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
  public String toString() {
    if (null != source)
      return source.toString();

    return String.format(
      "lazy %s %s [type = %s]", kind.getText(), name, typeName); 
  }

  private void checkSourceAssigned() {
    if (null == source) {
      throw new IllegalStateException(String.format(
        "Source for %s is not assigned.", name));
    }
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}
