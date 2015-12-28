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

package ru.ispras.microtesk.model.api.instruction;

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.Location;

/**
 * The AddressingModeImm class is a stub class that implements the immediate addressing mode. It
 * allows specifying immediate parameters of an instruction in the same way as with mode parameters.
 * Basically, a constant value parameter is represented by a built-in immediate addressing mode that
 * provides access to the read-only location that sores the data.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AddressingModeImm extends AddressingMode {
  public static final String NAME = "#IMM";
  public static final String PARAM_NAME = "value";

  private static final class Info extends InfoAndRule {
    Info(final Type type) {
      super(
          AddressingModeImm.class,
          NAME,
          type,
          new ParamDecls().declareParam(PARAM_NAME, type),
          false,
          false,
          false,
          false,
          0
          );
    }

    @Override
    public IAddressingMode create(final Map<String, Data> args) {
      final Location value = getArgument(PARAM_NAME, args);
      return new AddressingModeImm(value);
    }
  }

  public static IInfo INFO(final Type type) {
    return new Info(type);
  }

  private final Location value;

  public AddressingModeImm(final Location value) {
    this.value = value;
  }

  @Override
  public String syntax() {
    assert false : "Must not be called!";
    return null;
  }

  @Override
  public String image() {
    assert false : "Must not be called!";
    return null;
  }

  @Override
  public void action() {
    // NOTHING
    assert false : "Must not be called!";
  }

  @Override
  public Location access() {
    return value;
  }
}
