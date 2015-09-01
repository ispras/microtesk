/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * REG.java, Nov 20, 2012 12:24:58 PM Andrei Tatarnikov
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

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.R;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.byte_t;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.nibble;

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;

/*
 * mode REG(i: nibble)=R[i] syntax = format("R%d", i) image = format("01%4b", i)
 */

public final class REG extends AddressingMode {
  private static final class Info extends InfoAndRule {
    Info() {
      super(
          REG.class,
          "REG",
          byte_t,
          new ParamDecls()
               .declareParam("i", nibble),
          false,
          false,
          false,
          false,
          0
          );
    }

    @Override
    public IAddressingMode create(final Map<String, Data> args) {
      final Location i = getArgument("i", args);
      return new REG(i);
    }
  }

  public static final IInfo INFO = new Info();

  private Location i;

  public REG(final Location i) {
    this.i = i;
  }

  @Override
  public String syntax() {
    return String.format("R%d", i.getValue());
  }

  @Override
  public String image() {
    // TODO: NOT SUPPORTED
    // image = format("01%4b", i)
    return null;
  }

  public void action() {
    // NOTHING
  }

  @Override
  public Location access() {
    return R.access(i.load());
  }
}
