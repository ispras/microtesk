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

package ru.ispras.microtesk.model.api.metadata;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

/**
 * The MetaShortcut class describes a shortcut way to refer to an operation in some specific
 * context. A shortcut is composition of operations. Shortcuts can be used when there is a unique
 * way to build a composite object. The context is the name of the operation to be parameterized
 * with a shortcut object.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MetaShortcut implements MetaData {
  private final String contextName;
  private final MetaOperation operation;

  /**
   * Creates a shortcut object.
   * 
   * @param contextName Context identifier.
   * @param operation Description of the shortcut operation signature.
   * 
   * @throws IllegalArgumentException if any of the arguments is {@code null}.
   */

  public MetaShortcut(
      final String contextName,
      final MetaOperation operation) {
    checkNotNull(contextName);
    checkNotNull(operation);

    this.contextName = contextName;
    this.operation = operation;
  }

  /**
   * Returns the shortcut name.
   * 
   * @return The shortcut name.
   */

  @Override
  public String getName() {
    return operation.getName();
  }

  /**
   * Returns the context identifier that describes the operation that can be parameterized with (can
   * refer to) the given shortcut operation.
   * 
   * @return Name of the context in which the shortcut can be referred.
   */

  public String getContextName() {
    return contextName;
  }

  /**
   * Returns a metadata object describing the signature of the shortcut operation.
   * 
   * @return Metadata describing the shortcut operation.
   */

  public MetaOperation getOperation() {
    return operation;
  }
}
