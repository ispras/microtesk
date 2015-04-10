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

import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.Pair;
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

public final class MmuSpecBuilder implements TranslatorHandler<Ir> {
  public static final MmuAction START = new MmuAction("START");
  public static final MmuAction STOP = new MmuAction("STOP");

  private MmuSpecification spec = null;
  private VariableTracker variables = null;

  public MmuSpecification getSpecification() {
    return spec;
  }

  @Override
  public void processIr(final Ir ir) {
    System.out.println(ir);

    this.spec = new MmuSpecification();
    this.variables = new VariableTracker();

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
    registerControlFlowForMemory(memory);

    System.out.println("---------------------------------");
    System.out.println(spec);
  }

  private void registerAddress(final Address address) {
    final IntegerVariable addressVariable = 
        new IntegerVariable(address.getId(), address.getBitSize());

    variables.defineVariable(addressVariable);
    spec.registerAddress(new MmuAddress(addressVariable));
  }

  private void registerDevice(final Buffer buffer) {
    final MmuAddress address = spec.getAddress(buffer.getAddress().getId());
    final boolean isReplaceable = PolicyId.NONE != buffer.getPolicy();

    final String addressArgName = buffer.getAddressArg().getId();
    variables.defineVariableAs(address.getAddress(), addressArgName);

    try {
      final AddressFormatExtractor addressFormat = new AddressFormatExtractor(
          variables, address.getAddress(), buffer.getIndex(), buffer.getMatch());

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
        final IntegerVariable fieldVar = new IntegerVariable(field.getId(), field.getBitSize());
        device.addField(fieldVar);
      }

      spec.registerDevice(device);
      variables.defineGroup(device.getName(), device.getFields());
    } finally {
      variables.undefineVariable(addressArgName);
    }
  }

  private void registerControlFlowForMemory(Memory memory) {
    final MmuAddress address = spec.getAddress(memory.getAddress().getId());
    spec.setStartAddress(address);

    registerMemoryVariables(address.getAddress(), memory);
    System.out.println(variables);

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

    System.out.println("-----------------READ----------------");
    final Attribute readAttr = memory.getAttribute(AbstractStorage.READ_ATTR_NAME);
    registerControlFlow(START, readAttr.getStmts());

    System.out.println("----------------WRITE----------------");
    final Attribute writeAttr = memory.getAttribute(AbstractStorage.WRITE_ATTR_NAME);
    registerControlFlow(START, writeAttr.getStmts());
  }

  private void registerMemoryVariables(IntegerVariable address, Memory memory) {
    final Variable addressArg = memory.getAddressArg();
    final Variable dataArg = memory.getDataArg();

    variables.defineVariableAs(address, addressArg.getId());
    variables.defineVariable(new IntegerVariable(dataArg.getId(), dataArg.getBitSize()));

    for(Variable variable : memory.getVariables()) {
      variables.defineVariable(variable);
    }
  }

  private void registerControlFlow(final MmuAction source, final List<Stmt> stmts) {
    MmuAction current = source;

    for (Stmt stmt : stmts) {
      switch(stmt.getKind()) {
        case ASSIGN:
          current = registerAssignment(current, (StmtAssign) stmt);
          break;

        case EXCEPT:
          registerException(current, (StmtException) stmt);
          // Control flow cannot be continued after exception.
          return;

        case IF:
          current = registerIf(current, (StmtIf) stmt);
          break;

        case TRACE: // Ignored
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }
    }
  }

  private MmuAction registerAssignment(final MmuAction source, final StmtAssign stmt) {
    final Node lhs = stmt.getLeft();
    final Node rhs = stmt.getRight();

    final String name = String.format("%s = %s", lhs, rhs);
    System.out.println(name);

    if (lhs.getKind() != Node.Kind.VARIABLE) {
      throw new IllegalStateException("Left-hand side must be a variable expression: " + lhs);
    }

    System.out.println("!!! " + lhs.getUserData());
    System.out.println("!!! " + lhs.getUserData().getClass());

    System.out.println(variables.checkDefined(((NodeVariable) lhs).getName()));

    /*final IntegerVariable variable = getVariable(stmt.getLeft());
    final MmuExpression expression = getExpression(stmt.getRight());

    final MmuAssignment assignment = new MmuAssignment(variable, expression);
    
    final MmuAction targetAction = new MmuAction(name, assignment);
    spec.registerAction(targetAction);

    final MmuTransition transition = new MmuTransition(sourceAction, targetAction);
    spec.registerTransition(transition);*/
    
    // TODO
    return source;
  }

  private MmuAction registerIf(final MmuAction source, final StmtIf stmt) {
    System.out.println(stmt);
    
    for (Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      
    }
    
    // TODO Auto-generated method stub
    return source;
  }

  private void registerException(final MmuAction source, final StmtException stmt) {
    final String name = stmt.getMessage();

    final MmuAction target = new MmuAction(name);
    spec.registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);
  }

  private IntegerVariable getVariable(Node expr) {
    return null;
  }

  private MmuExpression getExpression(Node expr) {
    return MmuExpression.ZERO();
  }
}
