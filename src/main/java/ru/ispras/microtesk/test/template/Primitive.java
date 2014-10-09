/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Aug 26, 2014 8:11:50 PM Andrei Tatarnikov
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

public final class Primitive {
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

  private final Kind kind;
  private final String name;
  private final String typeName;
  private final boolean isRoot;
  private final Map<String, Argument> args;
  private final String contextName;
  private final Situation situation;

  Primitive(Kind kind, String name, String typeName, boolean isRoot, Map<String, Argument> args,
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

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
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
}
