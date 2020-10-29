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

class Field extends Lvalue {
  public final Lvalue base;
  public final String name;

  public Field(final Lvalue base, final String name) {
    this.base = base;
    this.name = name;
  }

  @Override
  public MirTy getType() {
    final MirStruct type = (MirStruct) base.getType();
    final TyRef tref = type.fields.get(name);

    return tref.type;
  }

  @Override
  public MirTy getContainerType() {
    return base.getContainerType();
  }

  @Override
  public String toString() {
    return String.format("%s.%s", base.toString(), name);
  }
}
