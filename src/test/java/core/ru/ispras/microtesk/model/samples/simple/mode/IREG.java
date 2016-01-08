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
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.R;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.byte_t;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.nibble;

import java.util.Map;

import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.ArgumentDecls;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;

/*
 * mode IREG(i: nibble) = M[R[i]]
 *   syntax = format("(R%d)", i)
 *   image = format("00%4b", i)
 */

public class IREG extends AddressingMode {
  private static final class Info extends InfoAndRule {
    Info() {
      super(
          IREG.class,
          "IREG",
          byte_t,
          new ArgumentDecls()
              .add("i", nibble),
          false,
          true,
          false,
          false,
          0
          );
    }

    @Override
    public IAddressingMode create(final Map<String, Object> args) {
      final Location i = getArgument("i", args);
      return new IREG(i);
    }
  }

  public static final IInfo INFO = new Info();

  private Location i;

  public IREG(Location i) {
    this.i = i;
  }

  @Override
  public String syntax() {
    return String.format("(R%d)", i.getValue());
  }

  @Override
  public String image() {
    // TODO: NOT SUPPORTED
    // image = format("00%4b", i)
    return null;
  }

  public void action() {
    // NOTHING
  }

  @Override
  public Location access() {
    return M.access(R.access(i.load()).load());
  }
}
