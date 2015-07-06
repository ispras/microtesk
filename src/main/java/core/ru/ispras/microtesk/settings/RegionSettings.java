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

package ru.ispras.microtesk.settings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.BigIntegerUtils;

/**
 * {@link RegionSettings} represents a configuration of a single memory region.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RegionSettings extends AbstractSettings {
  public static final String TAG = "region";

  public static enum Type {
    TEXT,
    DATA
  }

  public static class Mode {
    public final boolean r;
    public final boolean w;
    public final boolean x;

    public Mode(final boolean r, final boolean w, final boolean x) {
      this.r = r;
      this.w = w;
      this.x = x;
    }

    public Mode(final String rwx) {
      InvariantChecks.checkNotNull(rwx);
      InvariantChecks.checkTrue(rwx.length() == 3);

      final String mode = rwx.toLowerCase();

      this.r = mode.charAt(0) == 'r';
      this.w = mode.charAt(0) == 'w';
      this.x = mode.charAt(0) == 'x';
    }

    @Override
    public String toString() {
      return String.format("%s%s%s", (r ? "r" : "-"), (w ? "w" : "-"), (x ? "x" : "-"));
    }
  }

  private final String name;
  private final Type type;
  private final long startAddress;
  private final long endAddress;
  private final Mode mode;
  private final Mode others;

  private final Collection<AccessSettings> accesses = new ArrayList<>();

  public RegionSettings(
      final String name,
      final Type type,
      final long startAddress,
      final long endAddress,
      final Mode mode,
      final Mode others) {
    super(TAG);

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(others);

    this.name = name;
    this.type = type;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.mode = mode;
    this.others = others;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public long getStartAddress() {
    return startAddress;
  }

  public long getEndAddress() {
    return endAddress;
  }

  public boolean canRead() {
    return mode.r;
  }

  public boolean canWrite() {
    return mode.w;
  }

  public boolean canExecute() {
    return mode.x;
  }

  public boolean isEnabled() {
    return mode.r || mode.w || mode.x;
  }

  public boolean isVolatile() {
    return others.w;
  }

  public boolean checkAddress(final long address) {
    final BigInteger addr = BigIntegerUtils.valueOfUnsignedLong(address);
    final BigInteger startAddr = BigIntegerUtils.valueOfUnsignedLong(startAddress);
    final BigInteger endAddr = BigIntegerUtils.valueOfUnsignedLong(endAddress);

    return (startAddr.compareTo(addr) <= 0 && endAddr.compareTo(addr) >= 0);
  }

  public Collection<AccessSettings> getAccesses() {
    return accesses;
  }

  @Override
  public Collection<AbstractSettings> get(final String tag) {
    InvariantChecks.checkTrue(AccessSettings.TAG.equals(tag));

    final Collection<AbstractSettings> result = new ArrayList<>(accesses.size());
    result.addAll(getAccesses());

    return result;
  }

  @Override
  public void add(final AbstractSettings section) {
    InvariantChecks.checkTrue(section instanceof AccessSettings);

    accesses.add((AccessSettings) section);
  }

  @Override
  public String toString() {
    return String.format("%s={name=%s, type=%s, start=%x, end=%x, mode=%s%s}",
        TAG, name, type.name(), startAddress, endAddress, mode, others);
  }
}
