/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Value;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;

import java.math.BigInteger;

/**
 * The {@link Reader} class allows reading data from the microprocessor
 * register and memory directly or via addressing modes. This features
 * helps integrate models created by MicroTESK plugins with the ISA
 * model.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Reader {
  private static Model model = null;

  public static void setModel(final Model model) {
    InvariantChecks.checkNotNull(model);
    Reader.model = model;
  }

  public static Value<BitVector> fromMemory(
      final String name,
      final BigInteger index) {
    return new LocationValue(name, index);
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

    final IsaPrimitive mode;
    try {
      final IsaPrimitiveBuilder modeBuilder = model.newMode(name);

      int argIndex = 0;
      for (final String argName : metaMode.getArgumentNames()) {
        final BigInteger argValue = args[argIndex++];
        modeBuilder.setArgument(argName, argValue);
      }

      mode = modeBuilder.build();
    } catch (final ConfigurationException e) {
      throw new IllegalArgumentException(e);
    }

    return new ModeValue(mode);
  }

  private static final class LocationValue implements Value<BitVector> {
    private final String name;
    private final BigInteger index;

    private LocationValue(final String name, final BigInteger index) {
      InvariantChecks.checkNotNull(name);
      InvariantChecks.checkNotNull(index);

      this.name = name;
      this.index = index;
    }

    private LocationAccessor getLocation() {
      InvariantChecks.checkNotNull(model, "Model is not initialized!");

      try {
        return model.getPE().accessLocation(name, index);
      } catch (final ConfigurationException e) {
        throw new IllegalArgumentException(e);
      }
    }

    @Override
    public BitVector value() {
      final LocationAccessor location = getLocation();
      return BitVector.unmodifiable(
          BitVector.valueOf(location.getValue(), location.getBitSize()));
    }
  }

  private static final class ModeValue implements Value<BitVector> {
    private final IsaPrimitive mode;

    private ModeValue(final IsaPrimitive mode) {
      InvariantChecks.checkNotNull(mode);
      this.mode = mode;
    }

    @Override
    public BitVector value() {
      final Location location = mode.access(model.getPE(), model.getTempVars());
      return location.toBitVector();
    }
  }
}
