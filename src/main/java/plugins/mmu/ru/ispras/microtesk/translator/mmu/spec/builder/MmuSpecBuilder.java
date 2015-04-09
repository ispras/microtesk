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

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.model.api.mmu.PolicyId;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.mmu.ir.AbstractStorage;
import ru.ispras.microtesk.translator.mmu.ir.Address;
import ru.ispras.microtesk.translator.mmu.ir.Attribute;
import ru.ispras.microtesk.translator.mmu.ir.Buffer;
import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Ir;
import ru.ispras.microtesk.translator.mmu.ir.Memory;
import ru.ispras.microtesk.translator.mmu.ir.Stmt;
import ru.ispras.microtesk.translator.mmu.ir.StmtAssign;
import ru.ispras.microtesk.translator.mmu.ir.StmtException;
import ru.ispras.microtesk.translator.mmu.ir.StmtIf;
import ru.ispras.microtesk.translator.mmu.ir.Variable;
import ru.ispras.microtesk.translator.mmu.spec.MmuAction;
import ru.ispras.microtesk.translator.mmu.spec.MmuAddress;
import ru.ispras.microtesk.translator.mmu.spec.MmuAssignment;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.MmuGuard;
import ru.ispras.microtesk.translator.mmu.spec.MmuSpecification;
import ru.ispras.microtesk.translator.mmu.spec.MmuTransition;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;
import ru.ispras.microtesk.translator.mmu.spec.basis.MemoryOperation;

/**
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 */

public class MmuSpecBuilder implements TranslatorHandler<Ir> {
  public static final MmuAction START = new MmuAction("START");
  public static final MmuAction STOP = new MmuAction("STOP");

  private MmuSpecification spec = null;
  private MmuAction sourceAction = null;
  private Map<String, IntegerVariable> variables = null;

  @Override
  public void processIr(Ir ir) {
    System.out.println(ir);

    this.spec = new MmuSpecification();
    this.sourceAction = START;
    this.variables = new LinkedHashMap<>();

    for (Address address : ir.getAddresses().values()) {
      registerAddress(address);
    }

    for (Buffer buffer : ir.getBuffers().values()) {
      registerDevice(buffer);
    }

    final Map<String, Memory> memories = ir.getMemories();
    if (memories.size() > 1) {
      throw new IllegalStateException("Only one load/store specification is allowed.");
    }

    final Memory memory = memories.values().iterator().next();
    registerControlFlow(memory);

    System.out.println("---------------------------------");
    System.out.println(spec);
  }

  private void registerAddress(Address address) {
    final IntegerVariable variable = new IntegerVariable(address.getId(), address.getBitSize());
    spec.registerAddress(new MmuAddress(variable));
  }

  private void registerDevice(Buffer buffer) {
    final MmuAddress address = spec.getAddress(buffer.getAddress().getId());
    final boolean isReplaceable = PolicyId.NONE != buffer.getPolicy();

    final AddressFormatExtractor addressFormat = new AddressFormatExtractor(
        address, buffer.getAddressArg(), buffer.getIndex(), buffer.getMatch());

    final MmuDevice device = new MmuDevice(
        buffer.getId(),
        buffer.getWays(),
        buffer.getSets(),
        address,
        addressFormat.getTagExpr(),
        addressFormat.getIndexExpr(),
        addressFormat.getOffsetExpr(),
        isReplaceable
        );

    for(Field field : buffer.getEntry().getFields()) {
      device.addField(new IntegerVariable(field.getId(), field.getBitSize()));
    }

    spec.registerDevice(device);
  }

  private void registerControlFlow(Memory memory) {
    // Variables:
    
    // 1. address
    // 2. data
    // 3. local variables
    
    for(Variable variable : memory.getVariables()) {
      System.out.println(variable);
      
    }

    final MmuAddress address = spec.getAddress(memory.getAddress().getId());
    spec.setStartAddress(address);

    final MmuAction ROOT = new MmuAction("ROOT", new MmuAssignment(address.getAddress()));
    spec.registerAction(ROOT);

    spec.registerAction(START);
    spec.setStartAction(START);
    spec.registerAction(STOP);

    final MmuTransition IF_READ = new MmuTransition(
        ROOT, START, new MmuGuard(MemoryOperation.LOAD));

    final MmuTransition IF_WRITE = new MmuTransition(
        ROOT, START, new MmuGuard(MemoryOperation.STORE));

    spec.registerTransition(IF_READ);
    spec.registerTransition(IF_WRITE);

    System.out.println("-------------- READ -----------------------");
    final Attribute readAttr = memory.getAttribute(AbstractStorage.READ_ATTR_NAME);
    registerControlFlowForAttribute(readAttr, START);

    System.out.println("-------------- WRITE-----------------------");
    final Attribute writeAttr = memory.getAttribute(AbstractStorage.WRITE_ATTR_NAME);
    registerControlFlowForAttribute(writeAttr, START);
  }

  private void registerControlFlowForAttribute(Attribute attribute, MmuAction start) {
    sourceAction = start;

    for (Stmt stmt : attribute.getStmts()) {

      switch(stmt.getKind()) {
        case ASSIGN: {
          registerAssignment((StmtAssign) stmt);
          break;
        }

        case EXCEPT: {
          registerException((StmtException) stmt);
          // Control flow cannot be continued after exception.
          return;
        }

        case EXPR: {
          break;
        }

        case IF: {
          registerIf((StmtIf) stmt);
          break;
        }

        case TRACE: {
          // Ignore
          break;
        }

        default: {
          break;
        }
      }

    }

    //
    //
    //
  }

  private void registerAssignment(StmtAssign stmt) {
    final String name = String.format("%s = %s", stmt.getLeft(), stmt.getRight());
    System.out.println(name);

    /*final IntegerVariable variable = getVariable(stmt.getLeft());
    final MmuExpression expression = getExpression(stmt.getRight());

    final MmuAssignment assignment = new MmuAssignment(variable, expression);
    final MmuAction targetAction = new MmuAction(name, assignment);
    spec.registerAction(targetAction);

    final MmuTransition transition = new MmuTransition(sourceAction, targetAction);
    spec.registerTransition(transition);*/
  }

  private void registerIf(StmtIf stmt) {
    System.out.println(stmt);
    
    // TODO Auto-generated method stub
  }

  private void registerException(StmtException stmt) {
    final MmuAction targetAction = new MmuAction(stmt.getMessage());
    spec.registerAction(targetAction);

    final MmuTransition transition = new MmuTransition(sourceAction, targetAction);
    spec.registerTransition(transition);
  }

  private IntegerVariable getVariable(Node expr) {
    return null;
  }

  private MmuExpression getExpression(Node expr) {
    return MmuExpression.ZERO();
  }
}
