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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public class Primitive {
  public static enum Kind {
    /** Addressing mode. */
    MODE,
    /** Operation. */
    OP,
    /** Immediate value. */
    IMM
  }

  private final String name;
  private final Kind kind;
  private final boolean isOrRule;
  private final Type returnType;
  private final Set<String> attrNames;
  private final Map<String, PrimitiveReference> parents;

  Primitive(
      final String name,
      final Kind kind,
      final boolean isOrRule,
      final Type returnType,
      final Set<String> attrNames) {
    this.name = name;
    this.kind = kind;
    this.isOrRule = isOrRule;
    this.returnType = returnType;
    this.attrNames = attrNames;
    this.parents = new HashMap<>();
  }

  Primitive(Primitive source) {
    this.name = source.name;
    this.kind = source.kind;
    this.isOrRule = source.isOrRule;
    this.returnType = source.returnType;
    this.attrNames = source.attrNames;
    this.parents = new HashMap<>();

    for (Map.Entry<String, PrimitiveReference> e : source.parents.entrySet()) {
      final String id = e.getKey();
      final PrimitiveReference ref = e.getValue();

      this.parents.put(id, new PrimitiveReference(ref.source, this, ref.refNames));
    }
  }

  /**
   * Registers a reference made from the parent primitive to the current primitive.
   * 
   * @param parent Parent primitive.
   * @param referenceName The name of the reference (parameter) made from the parent primitive to
   *        the current primitive.
   */
  public void addParentReference(PrimitiveAND parent, String referenceName) {
    final PrimitiveReference reference;
    if (parents.containsKey(parent.getName())) {
      reference = parents.get(parent.getName());
    } else {
      reference = new PrimitiveReference(parent, this);
      parents.put(parent.getName(), reference);
    }
    reference.addReference(referenceName);
  }

  public final String getName() {
    if (null != name) {
      return name;
    }

    if (Kind.IMM == kind) {
      return returnType.getJavaText();
    }

    assert false : "Primitive name is not defined.";
    return null;
  }

  public final Kind getKind() {
    return kind;
  }

  public final boolean isOrRule() {
    return isOrRule;
  }

  public final Type getReturnType() {
    return returnType;
  }

  public final Set<String> getAttrNames() {
    return attrNames;
  }

  /**
   * Checks whether the current primitive is a root primitive. A primitive is a root primitive if it
   * does not have parents.
   * 
   * @return true if it is a root primitive or false otherwise.
   */

  public final boolean isRoot() {
    return 0 == getParentCount();
  }

  /**
   * Returns the collection of primitives (parents) that make references to the current primitive
   * (have parameters of the corresponding type).
   * 
   * @return Collection of parent primitives.
   */

  public final Collection<PrimitiveReference> getParents() {
    return Collections.unmodifiableCollection(parents.values());
  }

  /**
   * Returns the number of primitives (parents) that make references to the current primitive (have
   * parameters of the corresponding type).
   * 
   * @return Parent count.
   */

  public final int getParentCount() {
    return parents.size();
  }

  /**
   * Returns the total number of references made to the current primitive from parent primitives
   * (the total number of parameters of all parent primitives which have the corresponding type).
   * 
   * @return Number of reference to this primitive from all its parents.
   */

  public final int getParentReferenceCount() {
    int count = 0;
    for (final PrimitiveReference ref : getParents()) {
      count += ref.getReferenceCount();
    }
    return count;
  }
}
