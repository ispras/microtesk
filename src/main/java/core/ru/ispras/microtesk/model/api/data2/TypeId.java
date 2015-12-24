/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data2;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link TypeId} enumeration stores the list of data types (ways to interpret raw data)
 * supported by the model. The data types are taken from the nML language.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum TypeId {

  INT (1, true) {
    @Override
    protected Type newType(final int... params) {
      checkParamCount(params.length);
      return new Type(this, params[0]);
    }

    @Override
    protected Operations getOperations() {
      return OperationsInteger.get();
    }
  },

  CARD (1, true) {
    @Override
    protected Type newType(final int... params) {
      checkParamCount(params.length);
      return new Type(this, params[0]);
    }

    @Override
    protected Operations getOperations() {
      return OperationsInteger.get();
    }
  },

  FLOAT (2, false) {
    @Override
    protected Type newType(final int... params) {
      checkParamCount(params.length);
      // 1 is added to make room for implicit sign bit
      final int bitSize = params[0] + params[1] + 1;
      return new Type(TypeId.FLOAT, bitSize, params[0], params[1]);
    }

    @Override
    protected Operations getOperations() {
      return OperationsFloat.get();
    }
  };

  private static final Map<String, TypeId> TYPES = new HashMap<>();
  static {
    for (final TypeId typeId : values()) {
      TYPES.put(typeId.getName(), typeId);
    }
  }

  private final int paramCount;
  private final boolean isInteger;

  private TypeId(final int paramCount, final boolean isInteger) {
    this.paramCount = paramCount;
    this.isInteger = isInteger;
  }

  protected final String getName() {
    return name().toLowerCase();
  }

  protected final boolean isInteger() {
    return isInteger;
  }

  public static TypeId fromName(final String name) {
    return TYPES.get(name);
  }

  protected abstract Type newType(final int... params);
  protected abstract Operations getOperations();

  protected final void checkParamCount(final int paramCount) {
    if (paramCount != this.paramCount) {
      throw new IllegalArgumentException(String.format(
          "Wrong parameter count %d for the %s type", paramCount, name()));
    }
  }
}
