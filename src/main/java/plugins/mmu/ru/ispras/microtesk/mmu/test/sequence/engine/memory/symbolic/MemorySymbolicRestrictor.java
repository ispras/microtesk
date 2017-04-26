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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.symbolic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuProgram;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link MemorySymbolicRestrictor} produces a constraint for a given memory access.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySymbolicRestrictor {
  private final MmuSubsystem memory = MmuPlugin.getSpecification();

  private final RegionSettings region;

  public MemorySymbolicRestrictor(final RegionSettings region) {
    // Region can be null.
    this.region = region;
  }

  public Collection<IntegerConstraint<IntegerField>> getConstraints(
      final MmuBufferAccess bufferAccess) {
    final MmuAddressInstance physAddrType = memory.getPhysicalAddress();
    final MmuAddressInstance addrType = bufferAccess.getAddress();
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final MemoryAccessContext context = bufferAccess.getContext();

    // Restrict the physical memory access.
    final boolean isTopLevel = context.getMemoryAccessStack().isEmpty();
    final boolean isPhysAddr = addrType.getName().equals(physAddrType.getName());

    if (region != null && isTopLevel && isPhysAddr) {
      return getConstraints(addrType, region);
    }

    // Restrict the memory-mapped buffer access.
    if (buffer.getKind() == MmuBuffer.Kind.MEMORY) {
      final GeneratorSettings settings = GeneratorSettings.get();
      InvariantChecks.checkNotNull(settings);

      return getConstraints(
          bufferAccess.getAddress(),
          settings.getMemory().getRegion(buffer.getName()));
    }

    return Collections.<IntegerConstraint<IntegerField>>emptyList();
  }

  public Collection<IntegerConstraint<IntegerField>> getConstraints() {
    final GeneratorSettings settings = GeneratorSettings.get();
    InvariantChecks.checkNotNull(settings);

    return getConstraints(
        memory.getVirtualAddress(),
        settings.getMemory().getRegion(memory.getName()));
  }

  private Collection<IntegerConstraint<IntegerField>> getConstraints(
      final MmuAddressInstance addrType,
      final RegionSettings region) {
    InvariantChecks.checkNotNull(addrType);
    InvariantChecks.checkNotNull(region);

    final IntegerRange range = new IntegerRange(region.getMin(), region.getMax());
    Logger.debug("Range constraint: %s in %s", addrType, range);

    return Collections.<IntegerConstraint<IntegerField>>singleton(
        MemoryAccessConstraints.RANGE(addrType, range));
  }

  public Collection<IntegerConstraint<IntegerField>> getConstraints(
      final boolean isStart,
      final MmuTransition transition,
      final MemoryAccessContext context) {
    final Collection<IntegerConstraint<IntegerField>> constraints = new ArrayList<>();

    if (isStart) {
      constraints.addAll(getConstraints());
    }

    for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
      constraints.addAll(getConstraints(bufferAccess));
    }

    return constraints;
  }

  public Collection<IntegerConstraint<IntegerField>> getConstraints(
      final boolean isStart,
      final MmuProgram program,
      final MemoryAccessContext context) {
    final Collection<IntegerConstraint<IntegerField>> constraints = new ArrayList<>();

    if (isStart) {
      constraints.addAll(getConstraints());
    }

    for (final MmuTransition transition : program.getTransitions()) {
      for (final MmuBufferAccess bufferAccess : transition.getBufferAccesses(context)) {
        constraints.addAll(getConstraints(bufferAccess));
      }
    }

    return constraints;
  }
}
