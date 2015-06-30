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

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.api.mmu.PolicyId;
import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;
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
import ru.ispras.microtesk.translator.mmu.spec.basis.MemoryOperation;

/**
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 */

public final class MmuSpecBuilder implements TranslatorHandler<Ir> {
  /** Action node where the control flow graph terminates if no exceptions are raised. */
  public static final MmuAction STOP = new MmuAction("STOP");

  private MmuSpecification spec = null;
  private IntegerVariableTracker variables = null;
  private AtomExtractor atomExtractor = null;

  // Data variable for MMU (assignments to it must be ignored when building the control flow)
  private IntegerVariable data = null;

  /** Index used in automatically generated action names to ensure their uniqueness. */
  private int actionIndex = 0;

  public MmuSpecification getSpecification() {
    return spec;
  }

  @Override
  public void processIr(final Ir ir) {
    System.out.println(ir);

    this.spec = new MmuSpecification();
    this.variables = new IntegerVariableTracker();
    this.atomExtractor = new AtomExtractor(variables);
    this.actionIndex = 0;

    for (final Address address : ir.getAddresses().values()) {
      registerAddress(address);
    }

    for (final Buffer buffer : ir.getBuffers().values()) {
      registerDevice(buffer);
    }

    final Map<String, Memory> memories = ir.getMemories();
    if (memories.size() > 1) {
      throw new IllegalStateException("Only one load/store specification is allowed.");
    }

    final Memory memory = memories.values().iterator().next();

    final MmuAddress address = spec.getAddress(memory.getAddress().getId());
    spec.setStartAddress(address);
    variables.defineVariableAs(address.getAddress(), memory.getAddressArg().getId());

    data = new IntegerVariable(memory.getDataArg().getId(), memory.getDataArg().getBitSize());
    variables.defineVariable(data);

    for(final Variable variable : memory.getVariables()) {
      variables.defineVariable(variable);
    }

    registerControlFlowForMemory(memory);

    System.out.println("---------------------------------");
    System.out.println(spec);
  }

  private MmuAction newBranch(String text) {
    return new MmuAction(String.format("Branch_%d[%s]", actionIndex++, text));
  }

  private MmuAction newJoin() {
    return new MmuAction(String.format("Join_%d", actionIndex++));
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

      final MmuDevice parentDevice = (null != buffer.getParent()) ?
          spec.getDevice(buffer.getParent().getId()) : null;

      final MmuDevice device = new MmuDevice(
          buffer.getId(),
          buffer.getWays(),
          buffer.getSets(),
          address,
          addressFormat.getTagExpr(),
          addressFormat.getIndexExpr(),
          addressFormat.getOffsetExpr(),
          isReplaceable,
          parentDevice
          );

      for(final Field field : buffer.getEntry().getFields()) {
        final IntegerVariable fieldVar = new IntegerVariable(field.getId(), field.getBitSize());
        device.addField(fieldVar);
      }

      spec.registerDevice(device);
      variables.defineGroup(new IntegerVariableGroup(device));
    } finally {
      variables.undefine(addressArgName);
    }
  }

  private void registerControlFlowForMemory(Memory memory) {
    final MmuAddress address = spec.getAddress(memory.getAddress().getId());

    final MmuAction root = new MmuAction("ROOT", new MmuAssignment(address.getAddress()));
    spec.registerAction(root);
    spec.setStartAction(root);

    final MmuAction start = new MmuAction("START");
    spec.registerAction(start);

    // The control flow graph terminates in the STOP node if no exceptions are raised.
    spec.registerAction(STOP);

    // The load part of the control flow graph
    spec.registerTransition(new MmuTransition(root, start, new MmuGuard(MemoryOperation.LOAD)));
    registerControlFlowForAttribute(start, memory, AbstractStorage.READ_ATTR_NAME);

    // The store part of the control flow graph
    spec.registerTransition(new MmuTransition(root, start, new MmuGuard(MemoryOperation.STORE)));
    registerControlFlowForAttribute(start, memory, AbstractStorage.WRITE_ATTR_NAME);
  }

  private void registerControlFlowForAttribute(
      final MmuAction source, 
      final Memory memory,
      final String attributeName) {
    final Attribute attribute = memory.getAttribute(attributeName);
    if (null == attribute) {
      throw new IllegalStateException(String.format(
          "Undefined attribute: %s.%s", memory.getId(), attributeName));
    }

    final MmuAction stop = registerControlFlow(source, attribute.getStmts());
    if (null != stop) {
      spec.registerTransition(new MmuTransition(stop, STOP));
    }
  }

  private MmuAction registerControlFlow(final MmuAction source, final List<Stmt> stmts) {
    MmuAction current = source;

    for (final Stmt stmt : stmts) {
      switch(stmt.getKind()) {
        case ASSIGN:
          current = registerAssignment(current, (StmtAssign) stmt);
          break;

        case EXCEPT:
          registerException(current, (StmtException) stmt);
          return null; // Control flow cannot be continued after exception.

        case IF:
          current = registerIf(current, (StmtIf) stmt);
          break;

        case TRACE: // Ignored
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }
    }

    return current;
  }

  private MmuAction registerAssignment(final MmuAction source, final StmtAssign stmt) {
    final Node left = stmt.getLeft();
    final Node right = stmt.getRight();

    // Assignments that use the "data" MMU variable are ignored.
    if (isDataVariable(left) || isDataVariable(right)) {
      return source;
    }

    final Atom lhs = atomExtractor.extract(left);
    if (Atom.Kind.VARIABLE != lhs.getKind() && 
        Atom.Kind.GROUP != lhs.getKind() &&
        Atom.Kind.FIELD != lhs.getKind()) {
      throw new IllegalArgumentException(left + " cannot be used as left side of assignment.");
    }

    Atom rhs = atomExtractor.extract(right);
    if (Atom.Kind.VALUE == rhs.getKind()) {
      if (BigInteger.ZERO.equals((BigInteger) rhs.getObject())) {
        rhs = Atom.newConcat(MmuExpression.ZERO());
      } else {
        // Non-zero constant assignments are ignored.
        return source;
      }
    }

    final String name = String.format("Assignment (%s = %s)", left, right);
    final AssignmentBuilder assignmentBuilder = new AssignmentBuilder(name, lhs, rhs);

    final MmuAction target = assignmentBuilder.build();
    spec.registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);

    return target;
  }

  private boolean isDataVariable(Node expr) {
    if (expr.getKind() != Node.Kind.VARIABLE) {
      return false;
    }

    final String name = ((NodeVariable) expr).getName();
    final IntegerVariable variable = variables.getVariable(name);

    return data.equals(variable);
  }

  private MmuAction registerIf(final MmuAction source, final StmtIf stmt) {
    final MmuAction join = newJoin();
    spec.registerAction(join);

    MmuAction current = source;

    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node condition = block.first;
      final List<Stmt> stmts = block.second;

      final GuardExtractor guardExtractor = 
          new GuardExtractor(spec, atomExtractor, condition);

      final MmuAction ifTrueStart = newBranch(condition.toString());
      spec.registerAction(ifTrueStart);

      final MmuGuard guardIfTrue = guardExtractor.getGuard();
      spec.registerTransition(new MmuTransition(current, ifTrueStart, guardIfTrue));

      final MmuAction ifTrueStop = registerControlFlow(ifTrueStart, stmts);
      if (null != ifTrueStop) {
        spec.registerTransition(new MmuTransition(ifTrueStop, join));
      }

      final MmuAction ifFalseStart = newBranch("not " + condition.toString());
      spec.registerAction(ifFalseStart);

      final MmuGuard guardIfFalse = guardExtractor.getNegatedGuard();
      spec.registerTransition(new MmuTransition(current, ifFalseStart, guardIfFalse));

      current = ifFalseStart;
    }

    current = registerControlFlow(current, stmt.getElseBlock());
    if (null != current) {
      spec.registerTransition(new MmuTransition(current, join));
    }

    return join;
  }

  private void registerException(final MmuAction source, final StmtException stmt) {
    final String name = stmt.getMessage();

    final MmuAction target = new MmuAction(name);
    spec.registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);
  }
}
