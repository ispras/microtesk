/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.metadata;

import java.util.Set;

/**
 * The MetaArgument class describes instruction arguments.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaArgument implements MetaData {
  private final String name;
  private final Set<String> typeNames;

  public MetaArgument(String name, Set<String> typeNames) {
    if (name == null) {
      throw new NullPointerException();
    }

    if (null == typeNames) {
      throw new NullPointerException();
    }

    this.name = name;
    this.typeNames = typeNames;
  }

  /**
   * Returns the name of the argument.
   * 
   * @return Argument name.
   */

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns an iterator for the collection of type names associated with the argument.
   * 
   * @return An Iterable object that refers to the collection of type names (e.g. addressing mode
   *         names).
   */

  public Iterable<String> getTypeNames() {
    return typeNames;
  }

  /**
   * Checks whether if the specified type is accepted for the argument.
   * 
   * @param typeName Type name.
   * @return {@code true} if the specified type is accepted for the argument of {@code false}
   *         otherwise.
   */

  public boolean isTypeAccepted(String typeName) {
    return typeNames.contains(typeName);
  }
}
