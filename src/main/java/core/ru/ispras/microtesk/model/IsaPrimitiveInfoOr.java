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

package ru.ispras.microtesk.model;

public final class IsaPrimitiveInfoOr extends IsaPrimitiveInfo {
  private final IsaPrimitiveInfo[] items;

  public IsaPrimitiveInfoOr(
      final IsaPrimitiveKind kind,
      final String name,
      final IsaPrimitiveInfo... items) {
    super(kind, name, items[0].getType());
    this.items = items;
  }

  @Override
  public final boolean isSupported(final IsaPrimitive primitive) {
    for (final IsaPrimitiveInfo info : items) {
      if (info.isSupported(primitive)) {
        return true;
      }
    }
    return false;
  }
}
