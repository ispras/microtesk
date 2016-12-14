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

package ru.ispras.microtesk.decoder;

import java.util.HashMap;
import java.util.Map;
import ru.ispras.microtesk.model.api.IsaPrimitive;

public final class DecoderResult {
  private final IsaPrimitive primitive;
  private final Map<String, IsaPrimitive> primitiveArguments;
  private final int bitSize;

  public DecoderResult(
      final IsaPrimitive primitive,
      final int bitSize) {
    this.primitive = primitive;
    this.primitiveArguments = new HashMap<>();
    this.bitSize = bitSize;
  }

  public IsaPrimitive getPrimitive() {
    return primitive;
  }

  public void addArgument(final String name, final IsaPrimitive value) {
    primitiveArguments.put(name, value);
  }

  public Map<String, IsaPrimitive> getArguments() {
    return primitiveArguments;
  }

  public int getBitSize() {
    return bitSize;
  }
}
