/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The {@link PrimitiveReference} class describes references to the current primitive made from
 * another primitive (parent). There may be several reference since a primitive (AND rule) can
 * have several parameters of the same type.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class PrimitiveReference {
  public final PrimitiveAND source;
  public final Primitive target;
  public final Set<String> refNames;

  /**
   * Constructs a reference made from the source (parent) primitive to the target primitive.
   *
   * @param source Source primitive.
   * @param target Target primitive.
   */
  public PrimitiveReference(final PrimitiveAND source, final Primitive target) {
    this(source, target, new LinkedHashSet<String>());
  }

  /**
   * Additional constructor for making modified copies.
   */
  public PrimitiveReference(
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
  public void addReference(final String referenceName) {
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
   * Returns names of the references (parameter names) made from the parent primitive to
   * the current primitive.
   *
   * @return Set of references names (corresponding to parameter names).
   */
  public Set<String> getReferenceNames() {
    return Collections.unmodifiableSet(refNames);
  }

  /**
   * Resolves the reference and returns the source primitive that has all references resolved.
   *
   * <p>To resolve a reference from source to target means to set all source arguments that
   * can point to the target (OR rules) to the specified target.
   *
   * @return Source primitive that has all references resolved.
   */
  public PrimitiveAND resolve() {
    PrimitiveAND result = source;
    for (final String refName : refNames) {
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
