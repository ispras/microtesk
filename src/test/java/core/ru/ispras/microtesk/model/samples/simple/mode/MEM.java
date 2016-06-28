/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.samples.simple.mode;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.M;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.byte_t;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.index;

import java.util.Map;

import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.ArgumentDecls;
import ru.ispras.microtesk.model.api.instruction.Immediate;
import ru.ispras.microtesk.model.api.instruction.Primitive;
import ru.ispras.microtesk.model.api.memory.Location;

/*
 * mode MEM(i: index)=M[i]
 *   syntax = format("(%d)", i)
 *   image = format("%6b", i)
 */

public class MEM extends AddressingMode {
  private static final class Info extends InfoAndRule {
    Info() {
      super(
          MEM.class,
          "MEM",
          byte_t,
          new ArgumentDecls()
             .add("i", index),
          false,
          true,
          false,
          false,
          0
          );
    }

    @Override
    public AddressingMode create(final Map<String, Primitive> args) {
      final Immediate i = (Immediate) getArgument("i", args);
      return new MEM(i);
    }
  }

  public static final IInfo INFO = new Info();

  private final Immediate i;

  public MEM(final Immediate i) {
    this.i = i;
  }

  @Override
  public String syntax() {
    return String.format("(%d)", i.access().getValue());
  }

  @Override
  public String image() {
    // TODO: NOT SUPPORTED
    // image = format("%6b", i)
    return null;
  }

  @Override
  public void action() {}

  @Override
  public Location access() {
    return M.access(i.access().load());
  }
}
