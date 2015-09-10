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

package ru.ispras.microtesk.model.api.memory;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link AddressTranslator} class performs translation of addresses from virtual
 * to physical and vice versa.
 * 
 * <p>Translation is based on known base virtual and physical addresses. Such simplified
 * translation is required when we need to access memory without calling complex address
 * translation logic (described in MMU specifications). Situations when this is needed
 * include: placing a data image to memory, reading or  writing data to memory via
 * {@code ModelStateObserver}, etc.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AddressTranslator {
  private final BigInteger baseVirtualAddress;
  private final BigInteger basePhysicalAddress;
  private final boolean isTranslationNeeded;
  private final BitVector baseVirtualAddressBv;
  private final BitVector basePhysicalAddressBv;

  public AddressTranslator(
      final int addressBitSize,
      final BigInteger baseVirtualAddress,
      final BigInteger basePhysicalAddress) {
    InvariantChecks.checkGreaterThanZero(addressBitSize);
    InvariantChecks.checkNotNull(baseVirtualAddress);
    InvariantChecks.checkNotNull(basePhysicalAddress);

    this.baseVirtualAddress = baseVirtualAddress;
    this.basePhysicalAddress = basePhysicalAddress;
    this.isTranslationNeeded = !baseVirtualAddress.equals(basePhysicalAddress);

    if (isTranslationNeeded) {
      this.baseVirtualAddressBv = BitVector.valueOf(baseVirtualAddress, addressBitSize);
      this.basePhysicalAddressBv = BitVector.valueOf(basePhysicalAddress, addressBitSize);
    } else {
      this.baseVirtualAddressBv = null;
      this.basePhysicalAddressBv = null;
    }
  }

  public BigInteger virtualToPhysical(final BigInteger va) {
    InvariantChecks.checkNotNull(va);

    if (!isTranslationNeeded) {
      return va;
    }

    return basePhysicalAddress.add(va.subtract(baseVirtualAddress));
  }

  public BigInteger physicalToVirtual(final BigInteger pa) {
    InvariantChecks.checkNotNull(pa);

    if (!isTranslationNeeded) {
      return pa;
    }

    return baseVirtualAddress.add(pa.subtract(basePhysicalAddress));
  }

  public BitVector virtualToPhysical(final BitVector va) {
    InvariantChecks.checkNotNull(va);

    if (!isTranslationNeeded) {
      return va;
    }

    return BitVectorMath.add(
        basePhysicalAddressBv,
        BitVectorMath.sub(va, baseVirtualAddressBv)
        );
  }

  public BitVector physicalToVirtual(final BitVector pa) {
    InvariantChecks.checkNotNull(pa);

    if (!isTranslationNeeded) {
      return pa;
    }

    return BitVectorMath.add(
        baseVirtualAddressBv,
        BitVectorMath.sub(pa, basePhysicalAddressBv)
        );
  }

  @Override
  public String toString() {
    return String.format("AddressTranslator [baseVirtualAddress=%s, basePhysicalAddress=%s]",
        baseVirtualAddress, basePhysicalAddress);
  }
}
