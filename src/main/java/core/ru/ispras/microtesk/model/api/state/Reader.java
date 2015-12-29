/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.state;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Value;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;

/**
 * The {@link Reader} class allows reading data from the microprocessor
 * register and memory directly or via addressing modes. This features 
 * helps integrate models created by MicroTESK plugins with the ISA
 * model.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Reader {
  private static IModel model = null;

  public static void setModel(final IModel model) {
    InvariantChecks.checkNotNull(model);
    Reader.model = model;
  }

  public static Value<BitVector> fromMemory(
      final String name,
      final BigInteger index) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(index);

    final Memory memory = Memory.get(name);
    InvariantChecks.checkFalse(memory.getKind() == Memory.Kind.VAR);

    final Location location = memory.access(index);
    return new LocationValue(location);
  }

  public static Value<BitVector> fromMemory(final String name) {
    return fromMemory(name, BigInteger.ZERO);
  }

  public static Value<BitVector> fromAddressingMode(
      final String name,
      final BigInteger... args) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(model);

    final MetaAddressingMode metaMode = model.getMetaData().getAddressingMode(name);
    InvariantChecks.checkNotNull(metaMode);

    final IAddressingMode mode;
    try {
      final ICallFactory callFactory = model.getCallFactory();
      final IAddressingModeBuilder modeBuilder = callFactory.newMode(name);

      int argIndex = 0;
      for (final String argName : metaMode.getArgumentNames()) {
        final BigInteger argValue = args[argIndex++];
        modeBuilder.setArgumentValue(argName, argValue);
      }

      mode = modeBuilder.build();
    } catch (final ConfigurationException e) {
      throw new IllegalArgumentException(e);
    }

    return new ModeValue(mode);
  }

  private static final class LocationValue implements Value<BitVector> {
    private final Location location;

    private LocationValue(final Location location) {
      InvariantChecks.checkNotNull(location);
      this.location = location;
    }

    @Override
    public BitVector value() {
      return BitVector.unmodifiable(
          BitVector.valueOf(location.getValue(), location.getBitSize()));
    }
  }

  private static final class ModeValue implements Value<BitVector> {
    private final IAddressingMode mode;

    private ModeValue(final IAddressingMode mode) {
      InvariantChecks.checkNotNull(mode);
      this.mode = mode;
    }

    @Override
    public BitVector value() {
      final Location location = mode.access();
      return new LocationValue(location).value();
    }
  }
}
