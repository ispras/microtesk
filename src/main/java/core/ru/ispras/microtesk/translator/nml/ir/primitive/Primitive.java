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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.ArgumentMode;
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

  public static final class Holder {
    private Primitive value;
    private final Map<String, ArgumentMode> argsUsage;

    public Holder() {
      this.value = null;
      this.argsUsage = new HashMap<>();
    }

    public Holder(Primitive value) {
      assert value != null;
      this.value = value;
      this.argsUsage = new HashMap<>(); 
    }

    public void setValue(Primitive value) {
      assert null == this.value : "Aready assigned.";

      if (value instanceof PrimitiveAND) {
        ((PrimitiveAND) value).setArgsUsage(argsUsage);
      }

      this.value = value;
    }

    public Primitive getValue() {
      return value;
    }

    public void setArgsUsage(final String name, final ArgumentMode usage) {
      final ArgumentMode prevUsage = argsUsage.get(name);
      if (usage == ArgumentMode.IN && prevUsage == ArgumentMode.OUT) {
        argsUsage.put(name, ArgumentMode.INOUT);
      } else if (usage == ArgumentMode.OUT && prevUsage == ArgumentMode.IN) {
        argsUsage.put(name, ArgumentMode.INOUT);
      } else {
        argsUsage.put(name, usage);
      }
    }
  }

  /**
   * The Reference class describes references to the current primitive made from another primitive
   * (parent). There may be several reference since a primitive (AND rule) can have several
   * parameters of the same type.
   * 
   * @author Andrei Tatarnikov
   */

  public static final class Reference {
    private final PrimitiveAND source;
    private final Primitive target;
    private final Set<String> refNames;

    /**
     * Constructs a reference made from the source (parent) primitive to the target primitive.
     */

    private Reference(PrimitiveAND source, Primitive target) {
      this(source, target, new LinkedHashSet<String>());
    }

    /**
     * Additional constructor for making modified copies.
     */

    private Reference(
        final PrimitiveAND source,
        final Primitive target,
        final Set<String> refNames) {
      this.source = source;
      this.target = target;
      this.refNames = refNames;
    }

    /**
     * Registers a reference from the parent primitive to the current primitive.
     */

    private void addReference(String referenceName) {
      refNames.add(referenceName);
    }

    /**
     * Returns the name of the primitive that makes a reference to the current primitive.
     */

    public String getName() {
      return source.getName();
    }

    /**
     * Returns the primitive the refers to the current primitive.
     */

    public PrimitiveAND getSource() {
      return source;
    }

    /**
     * Returns the primitive the reference points to (current primitive).
     */

    public Primitive getTarget() {
      return target;
    }

    /**
     * Returns the number of references made from the parent primitive to the current primitive.
     */

    public int getReferenceCount() {
      return refNames.size();
    }

    /**
     * Returns names of the references (parameter names) made from the parent primitive
     * to the current primitive.
     */

    public Set<String> getReferenceNames() {
      return Collections.unmodifiableSet(refNames);
    }

    /**
     * Resolves the reference and returns the source primitive that has all references resolved. To
     * resolve a reference from source to target means to set all source arguments that can point to
     * the target (OR rules) to the specified target.
     */

    public PrimitiveAND resolve() {
      PrimitiveAND result = source;
      for (String refName : refNames) {
        if (result.getArguments().get(refName) != target) {
          if (result == source) {
            result = source.makeCopy();
          }
          result.getArguments().put(refName, target);
        }
      }
      return result;
    }
  }

  private final String name;
  private final Kind kind;
  private final boolean isOrRule;
  private final Type returnType;
  private final Set<String> attrNames;
  private final Map<String, Reference> parents;

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
    this.parents = new HashMap<String, Reference>();
  }

  Primitive(Primitive source) {
    this.name = source.name;
    this.kind = source.kind;
    this.isOrRule = source.isOrRule;
    this.returnType = source.returnType;
    this.attrNames = source.attrNames;
    this.parents = new HashMap<String, Reference>();

    for (Map.Entry<String, Reference> e : source.parents.entrySet()) {
      final String id = e.getKey();
      final Reference ref = e.getValue();

      this.parents.put(id, new Reference(ref.source, this, ref.refNames));
    }
  }

  /**
   * Registers a reference made from the parent primitive to the current primitive.
   * 
   * @param parent Parent primitive.
   * @param referenceName The name of the reference (parameter) made from the parent primitive to
   *        the current primitive.
   */
  protected void addParentReference(PrimitiveAND parent, String referenceName) {
    final Reference reference;
    if (parents.containsKey(parent.getName())) {
      reference = parents.get(parent.getName());
    } else {
      reference = new Reference(parent, this);
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

  public final Collection<Reference> getParents() {
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
    for (Reference ref : getParents()) {
      count += ref.getReferenceCount();
    }
    return count;
  }
}
