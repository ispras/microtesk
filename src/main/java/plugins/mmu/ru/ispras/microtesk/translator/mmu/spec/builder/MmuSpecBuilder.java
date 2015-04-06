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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.mmu.ir.Address;
import ru.ispras.microtesk.translator.mmu.ir.Buffer;
import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.ir.Memory;
import ru.ispras.microtesk.translator.mmu.spec.MmuAction;
import ru.ispras.microtesk.translator.mmu.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.MmuSpecification;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 */

public class MmuSpecBuilder implements TranslatorHandler<Ir> {
  public static final MmuAction START = new MmuAction("START");

  private MmuSpecification spec; 
  private Map<String, MmuAddress> addresses;

  @Override
  public void processIr(Ir ir) {
    System.out.println(ir);
    
    this.spec = new MmuSpecification();
    this.addresses = new HashMap<>();

    registerAddresses(ir.getAddresses().values());
    registerDevices(ir.getBuffers().values());

    for (Memory memory : ir.getMemories().values()) {
      final MmuAddress address = addresses.get(memory.getAddress().getId());
      spec.setStartAddress(address);
    }

    spec.registerAction(START);
    spec.setStartAction(START);

    System.out.println(spec);
  }

  private void registerAddresses(Collection<Address> values) {
    for (Address a : values) {
      final IntegerVariable variable = new IntegerVariable(a.getId(), a.getBitSize());
      final MmuAddress address = new MmuAddress(variable);

      spec.registerAddress(address);
      addresses.put(a.getId(), address);
    }
  }

  private void registerDevices(Collection<Buffer> buffers) {
    for (Buffer buffer : buffers) {
      final MmuAddress address = addresses.get(buffer.getAddress().getId());

      final MmuDevice device = new MmuDevice(
          buffer.getId(),
          buffer.getWays(),
          buffer.getSets(),
          address,
          getTagExpr(buffer.getMatch()),
          getIndexExpr(buffer.getIndex()),
          MmuExpression.ZERO(), // TODO offsetExpression,
          true /*TODO: replaceable - ??? */);

      for(Field field : buffer.getEntry().getFields()) {
        device.addField(new IntegerVariable(field.getId(), field.getBitSize()));
      }

      spec.registerDevice(device);
    }
  }

  private MmuExpression getIndexExpr(Node index) {
    // System.out.println(index);
    // TODO Auto-generated method stub
    return MmuExpression.ZERO();
  }

  private MmuExpression getTagExpr(Node match) {
    // System.out.println(match);
    // TODO Auto-generated method stub
    return MmuExpression.ZERO();
  }
}
