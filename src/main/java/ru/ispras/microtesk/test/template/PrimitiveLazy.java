/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveLazy.java, Oct 9, 2014 6:14:48 PM Andrei Tatarnikov
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

public final class PrimitiveLazy implements Primitive {
  private Primitive source;
  private final Kind kind;
  private final String name;
  private final String typeName;

  public PrimitiveLazy(Kind kind, String name, String typeName)
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
