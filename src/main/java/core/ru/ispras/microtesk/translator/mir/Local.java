/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

class Local implements Operand {
  public final int id;
  private final MirTy type;

  public Local(final int id, final MirTy type) {
    this.id =  id;
    this.type = type;
  }

  @Override
  public MirTy getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("%%%d", id);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Local) {
      final Local that = (Local) o;
      return this.id == that.id;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
