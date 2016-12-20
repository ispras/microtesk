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

package ru.ispras.microtesk.model.api;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.Location;

/**
 * {@link Immediate} is a primitive that describes immediate values.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Immediate extends IsaPrimitive {
  public static final String TYPE_NAME = "#IMM";

  private final Location location;

  public Immediate(final Location location) {
    super();
    InvariantChecks.checkNotNull(location);
    this.location = location;
  }

  public Immediate(final Data data) {
    super();
    this.location = Location.newLocationForConst(data);
  }

  public Immediate(final Type type) {
    this(new Data(type));
  }

  @Override
  public Location access(
      final ProcessingElement processingElement,
      final TemporaryVariables temporaryVariables) {
    return access();
  }

  public Location access() {
    return location;
  }

  public Type getType() {
    return location.getType();
  }

  public static final class Info extends IsaPrimitiveInfo {
    public Info(final Type type) {
      super(IsaPrimitiveKind.IMM, TYPE_NAME, type);
      InvariantChecks.checkNotNull(type);
    }

    @Override
    public boolean isSupported(final IsaPrimitive primitive) {
      return primitive instanceof Immediate &&
             getType().equals(((Immediate) primitive).getType());
    }
  }
}
