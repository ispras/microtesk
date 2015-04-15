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

import java.util.ArrayList;
import java.util.Collections;
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
import ru.ispras.microtesk.translator.mmu.ir.AttributeRef;
import ru.ispras.microtesk.translator.mmu.ir.Buffer;
import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.FieldRef;
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
  /** Action node where the control flow graph terminates if no exceptions are raised. */
  public static final MmuAction STOP = new MmuAction("STOP");

  private MmuSpecification spec = null;
  private IntegerVariableTracker variables = null;
  private AtomExtractor atomExtractor = null;

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
    registerDevice(memory);

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

      for(final Field field : buffer.getEntry().getFields()) {
        final IntegerVariable fieldVar = new IntegerVariable(field.getId(), field.getBitSize());
        device.addField(fieldVar);
      }

      spec.registerDevice(device);
      variables.defineGroup(device.getName(), device.getFields());
    } finally {
      variables.undefineVariable(addressArgName);
    }
  }

  private void registerDevice(final Memory memory) {
    final MmuAddress address = spec.getAddress(memory.getAddress().getId());

    // Main memory is fully associative
    final long ways = 1;
    final MmuExpression tag = MmuExpression.ZERO();

    // Temporary stubs
    final long sets = 0;
    final MmuExpression index = MmuExpression.ZERO();
    final MmuExpression offset = MmuExpression.ZERO();

    final MmuDevice device = new MmuDevice(
        memory.getId(), ways, sets, address, tag, index, offset, false);

    spec.registerDevice(device);
  }

  private void registerControlFlowForMemory(Memory memory) {
    final MmuAddress address = spec.getAddress(memory.getAddress().getId());
    spec.setStartAddress(address);

    registerMemoryVariables(address.getAddress(), memory);
    System.out.println(variables);

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

  private void registerMemoryVariables(IntegerVariable address, Memory memory) {
    final Variable addressArg = memory.getAddressArg();
    final Variable dataArg = memory.getDataArg();

    variables.defineVariableAs(address, addressArg.getId());
    variables.defineVariable(new IntegerVariable(dataArg.getId(), dataArg.getBitSize()));

    for(final Variable variable : memory.getVariables()) {
      variables.defineVariable(variable);
    }
  }

  private void registerControlFlowForAttribute(
      final MmuAction source, 
      final Memory memory,
      final String attributeName) {
    System.out.println("---------------- " + attributeName + " ----------------");

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

  private static class AssigmentActionBuilder {
    private final String name;

    private MmuDevice device = null;
    private List<IntegerVariable> left = null;
    private List<IntegerVariable> right = null;

    AssigmentActionBuilder(String name) {
      this.name = name;
    }

    public void setDevice(MmuDevice device) {
      this.device = device;
    }

    public void setLeftSide(IntegerVariable variable) {
      left = Collections.singletonList(variable);
    }

    public void setLeftSide(List<IntegerVariable> variables) {
      left = variables;
    }

    public void setLeftSide(Map<String, IntegerVariable> variables) {
      left = new ArrayList<>(variables.values());
    }

    public void setRightSide(IntegerVariable variable) {
      right = Collections.singletonList(variable);
    }

    public void setRightSide(List<IntegerVariable> variables) {
      right = variables;
    }

    public void setRightSide(Map<String, IntegerVariable> variables) {
      right = new ArrayList<>(variables.values());
    }

    public MmuAction build() {
      if (null == left) {
        throw new IllegalStateException("Left hand side is not defined.");
      }

      if (null == right) {
        throw new IllegalStateException("Right hand side is not defined.");
      }

      if (left.size() != right.size()) {
        throw new IllegalStateException("Assignment mismatch");
      }

      final MmuAssignment[] assignments = new MmuAssignment[left.size()];
      for (int index = 0; index < left.size(); ++index) {
        assignments[index] = new MmuAssignment(left.get(index), MmuExpression.VAR(right.get(index)));
      }

      return new MmuAction(name, device, assignments);
    }
  }

  private MmuAction registerAssignment(final MmuAction source, final StmtAssign stmt) {
    // System.out.println("### " + stmt.getLeft() + " -> " + atomExtractor.extract(stmt.getLeft()));
    //System.out.println("### " + stmt.getRight() + " -> " + atomExtractor.extract(stmt.getRight()));

    if (Node.Kind.VARIABLE != stmt.getLeft().getKind() ||
        Node.Kind.VARIABLE != stmt.getRight().getKind()) {
      // TODO: Currently, rhs and lhs can be variable expressions only
      System.out.println("!!! IGNORED " + stmt);
      return source;
    }

    final NodeVariable lhs = (NodeVariable) stmt.getLeft();
    final NodeVariable rhs = (NodeVariable) stmt.getRight();

    final String name = String.format("Assignment (%s = %s)", lhs, rhs);
    final AssigmentActionBuilder assigmentBuilder = new AssigmentActionBuilder(name);

    if (lhs.getUserData() instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) lhs.getUserData();
      final MmuDevice device = spec.getDevice(attrRef.getTarget().getId());
      assigmentBuilder.setDevice(device);
      assigmentBuilder.setLeftSide(device.getFields());
    } else if (lhs.getUserData() instanceof FieldRef) {
      final FieldRef fieldRef = (FieldRef) lhs.getUserData();
      final IntegerVariable intVar = 
          variables.getGroup(fieldRef.getVariable().getId()).get(fieldRef.getField().getId());
      assigmentBuilder.setLeftSide(intVar);
    } else {
      final IntegerVariableTracker.Status status = variables.checkDefined(lhs.getName());
      switch (status) {
        case VARIABLE:
          assigmentBuilder.setLeftSide(variables.getVariable(lhs.getName()));
          break;
        case GROUP:
          assigmentBuilder.setLeftSide(variables.getGroup(lhs.getName()));
          break;
        default:
          throw new IllegalStateException("Undeclared variable: " + lhs.getName());
      }
    }

    if (rhs.getUserData() instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) rhs.getUserData();
      final MmuDevice device = spec.getDevice(attrRef.getTarget().getId());
      assigmentBuilder.setDevice(device);
      assigmentBuilder.setRightSide(device.getFields());
    } else if (rhs.getUserData() instanceof FieldRef) {
      final FieldRef fieldRef = (FieldRef) rhs.getUserData();
      final IntegerVariable intVar = 
          variables.getGroup(fieldRef.getVariable().getId()).get(fieldRef.getField().getId());
      assigmentBuilder.setRightSide(intVar);
    } else {
      final IntegerVariableTracker.Status status = variables.checkDefined(rhs.getName());
      switch (status) {
        case VARIABLE:
          assigmentBuilder.setRightSide(variables.getVariable(rhs.getName()));
          break;
        case GROUP:
          assigmentBuilder.setRightSide(variables.getGroup(rhs.getName()));
          break;
        default:
          throw new IllegalStateException("Undeclared variable: " + rhs.getName());
      }
    }

    final MmuAction target = assigmentBuilder.build();
    spec.registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);

    return target;
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
