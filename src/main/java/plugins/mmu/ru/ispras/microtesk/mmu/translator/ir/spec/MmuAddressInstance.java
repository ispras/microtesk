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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

/**
 * {@link MmuAddressInstance} describes an address, i.e. a parameter used to access a buffer.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class MmuAddressInstance extends MmuStruct {
  /** Address description (the variable contains the name and the bit length). */
  private final Variable addrStruct;
  private IntegerVariable address;

  public MmuAddressInstance(
      final String name,
      final Variable addrStruct,
      final IntegerVariable address) {
    super(name);

    this.addrStruct = addrStruct;
    this.address = address;
  }

  public MmuAddressInstance(final String name) {
    super(name);

    this.addrStruct = null;
    this.address = null;
  }

  protected void setVariable(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkTrue(address == null);

    this.address = variable;
  }

  public final IntegerVariable getVariable() {
    return address;
  }

  public final int getWidth() {
    return getVariable().getWidth();
  }

  public final Variable getStruct() {
    return addrStruct;
  }

  // TODO:
  public MmuAddressInstance getInstance(final MemoryAccessContext context) {
    InvariantChecks.checkNotNull(context);

    final MmuAddressInstance instance = new MmuAddressInstance(name, addrStruct, address);

    for (final IntegerVariable field : fields) {
      instance.fields.add(context.getInstance(field));
    }

    instance.buffer = buffer;
    instance.bitSize = bitSize;
    instance.address = context.getInstance(address);

    return instance;
  }

  @Override
  public String toString() {
    return String.format("%s:%s[%d]", name, address.getName(), address.getWidth());
  }

  @Override
  public int hashCode() {
    return 31 * getName().hashCode() + (address != null ? address.hashCode() : 0);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || !(obj instanceof MmuAddressInstance)) {
      return false;
    }

    final MmuAddressInstance other = (MmuAddressInstance) obj;

    return name.equals(other.name)
        && ((address == null && other.address == null)
        ||  (address != null && address.equals(other.address)));
  }
}
