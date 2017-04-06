/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerRangeConstraint;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.utils.BigIntegerUtils;

/**
 * {@link MemoryAccessRestrictor} produces a constraint for a given memory access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessRestrictor {
  private final MmuSubsystem memory = MmuPlugin.getSpecification();

  private final RegionSettings region;
  private final Collection<IntegerConstraint<IntegerField>> constraints;

  public MemoryAccessRestrictor(
      final MmuSegment segment,
      final RegionSettings region,
      final Collection<IntegerConstraint<IntegerField>> constraints) {
    // Segment and region can be null.
    InvariantChecks.checkNotNull(constraints);

    this.region = region;
    this.constraints = new ArrayList<>(constraints);

    // Restrict the virtual memory access.
    if (segment != null) {
      constrain(memory.getVirtualAddress(), getRange(segment));
    }
  }

  public MemoryAccessRestrictor(
      final MmuSegment segment,
      final RegionSettings region) {
    this(segment, region, Collections.<IntegerConstraint<IntegerField>>emptyList());
  }

  public void constrain(final IntegerConstraint<IntegerField> constraint) {
    constraints.add(constraint);
  }

  public void constrain(final MmuAddressInstance address, final IntegerRange range) {
    constrain(new IntegerRangeConstraint(address.getVariable(), range));
  }

  public void constrain(final MmuAddressInstance address, final BigInteger value) {
    constrain(new IntegerDomainConstraint<IntegerField>(address.getVariable().field(), value));
  }

  public void constrain(final MmuBufferAccess bufferAccess) {
    final MmuAddressInstance physAddrType = memory.getPhysicalAddress();
    final MmuAddressInstance addrType = bufferAccess.getAddress();
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final MemoryAccessContext context = bufferAccess.getContext();

    // Restrict the physical memory access.
    final boolean isTopLevel = context.getMemoryAccessStack().isEmpty();
    final boolean isPhysAddr = addrType.getName().equals(physAddrType.getName());

    if (region != null && isTopLevel && isPhysAddr) {
      constrain(addrType, getRange(region));
    }

    // Restrict the memory-mapped buffer access.
    if (buffer.getKind() == MmuBuffer.Kind.MEMORY) {
      final GeneratorSettings settings = GeneratorSettings.get();
      InvariantChecks.checkNotNull(settings);
      final RegionSettings region = settings.getMemory().getRegion(buffer.getName());
      InvariantChecks.checkNotNull(region);

      constrain(bufferAccess.getAddress(), getRange(region));
    }
  }

  public final Collection<IntegerConstraint<IntegerField>> getConstraints() {
    return constraints;
  }

  private static IntegerRange getRange(final MmuSegment segment) {
    return new IntegerRange(
        BigIntegerUtils.valueOfUnsignedLong(segment.getMin()),
        BigIntegerUtils.valueOfUnsignedLong(segment.getMax())
    );
  }

  private static IntegerRange getRange(final RegionSettings region) {
    return new IntegerRange(
        BigIntegerUtils.valueOfUnsignedLong(region.getMin()),
        BigIntegerUtils.valueOfUnsignedLong(region.getMax())
    );
  }
}
