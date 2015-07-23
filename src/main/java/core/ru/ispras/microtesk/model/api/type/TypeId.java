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

/**
 * The TypeCreator interface describes the protocol for creating
 * a type object parameterized with a variable parameter count.
 * This is needed when a type is created from some external data
 * (e.g. provided by user in a test template). 
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

interface TypeCreator {
  Type createWithParams(int... params);
}

/**
 * The TypeId enumeration stores the list of data types (ways to interpret raw data) supported by
 * the model. The data types are taken from the nML language.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum TypeId implements TypeCreator {

  INT (1) {
    @Override
    public Type createWithParams(final int ... params) {
      checkParamCount(params.length);
      return new Type(this, params[0]);
    }
  },

  CARD (1) {
    @Override
    public Type createWithParams(final int... params) {
      checkParamCount(params.length);
      return new Type(this, params[0]);
    }
  },

  FLOAT(2) {
    @Override
    public Type createWithParams(final int... params) {
      checkParamCount(params.length);
      // 1 is added to make room for implicit sign bit
      final int bitSize = params[0] + params[1] + 1;
      return new Type(TypeId.FLOAT, bitSize, params[0], params[1]);
    }
  },

  FIX (2) {
    @Override
    public Type createWithParams(final int... params) {
      checkParamCount(params.length);
      final int bitSize = params[0] + params[1];
      return new Type(TypeId.FIX, bitSize, params[0], params[1]);
    }
  },

  // RANGE, // NOT SUPPORTED IN THIS VERSION
  // ENUM,  // NOT SUPPORTED IN THIS VERSION

  BOOL (1) {
    @Override
    public Type createWithParams(final int... params) {
      checkParamCount(params.length);
      return new Type(this, params[0]);
    }
  };

  private final int paramCount;
  private TypeId(int paramCount) {
    this.paramCount = paramCount;
  }

  void checkParamCount(final int paramCount) {
    if (paramCount != this.paramCount) {
      throw new IllegalArgumentException(String.format(
          "Wrong parameter count %d for the %s type", paramCount, name()));
    }
  }
}
