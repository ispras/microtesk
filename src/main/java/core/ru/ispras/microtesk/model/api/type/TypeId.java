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

package ru.ispras.microtesk.model.api.type;

import java.util.HashMap;
import java.util.Map;

/**
 * The TypeId enumeration stores the list of data types (ways to interpret raw data) supported by
 * the model. The data types are taken from the Sim-nML language.
 * 
 * @author Andrei Tatarnikov
 */

public enum TypeId {
  
  INT (1) {
    @Override
    public Type createWithParams(int ... params) {
      return new Type(this, params[0]);
    }
  },

  CARD (1) {
    @Override
    public Type createWithParams(int ... params) {
      return new Type(this, params[0]);
    }
  },

  FLOAT(2) {
    @Override
    public Type createWithParams(int ... params) {
      // 1 is added to make room for implicit sign bit
      final int bitSize = params[0] + params[1] + 1;
      return new Type(TypeId.FLOAT, bitSize, params[0], params[1]);
    }
  },

  FIX (2) {
    @Override
    public Type createWithParams(int ... params) {
      final int bitSize = params[0] + params[1];
      return new Type(TypeId.FIX, bitSize, params[0], params[1]);
    }
  },

  // RANGE, // NOT SUPPORTED IN THIS VERSION
  // ENUM,  // NOT SUPPORTED IN THIS VERSION

  BOOL (1) {
    @Override
    public Type createWithParams(int ... params) {
      return new Type(this, params[0]);
    }
  };

  private static final Map<String, TypeId> table = new HashMap<String, TypeId>();
  static {
    for (TypeId typeId : values()) {
      table.put(typeId.name(), typeId);
      table.put(typeId.name().toLowerCase(), typeId);
    }
  }

  private final int paramCount;
  private TypeId(int paramCount) {
    this.paramCount = paramCount;
  }

  public abstract Type createWithParams(int ... params);

  public static TypeId fromText(String text) {
    if (null == text) {
      throw new NullPointerException();
    }
    return table.get(text);
  }
  
  public static Type typeOf(TypeId typeId, int ... params) {
    if (null == typeId) {
      throw new NullPointerException();
    }
    if (params.length != typeId.paramCount) {
      throw new IllegalArgumentException(String.format(
          "Wrong parameter count %d for the %s type", params.length, typeId.name()));
    }
    return typeId.createWithParams(params);
  }

  public static Type typeOf(String name, int ... params) {
    final TypeId typeId = fromText(name);
    return typeOf(typeId, params);
  }
}
