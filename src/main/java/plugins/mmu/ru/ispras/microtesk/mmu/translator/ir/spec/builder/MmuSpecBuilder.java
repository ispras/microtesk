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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.model.api.PolicyId;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Attribute;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAssignment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.translator.TranslatorHandler;

import static ru.ispras.microtesk.mmu.translator.ir.spec.builder.ScopeStorage.dotConc;

/**
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 */

public final class MmuSpecBuilder implements TranslatorHandler<Ir> {
  /** Action node where the control flow graph terminates if no exceptions are raised. */
  public static final MmuAction STOP = new MmuAction("STOP");

  private MmuSubsystem.Builder spec = null;
  private MmuSpecContext context = null;
  private IntegerVariableTracker variables = null;
  private List<String> currentMarks = null;

  // Data variable for MMU (assignments to it must be ignored when building the control flow)
  private IntegerVariable data = null;

  /** Index used in automatically generated action names to ensure their uniqueness. */
  private int actionIndex = 0;

  public MmuSubsystem getSpecification() {
    return spec.build();
  }

  @Override
  public void processIr(final Ir ir) {
    System.out.println(ir);

    this.spec = new MmuSubsystem.Builder();
    this.context = new MmuSpecContext();
    this.variables = context.getVariableRegistry();
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

    final MmuAddressType address = spec.getAddress(memory.getAddress().getId());
    spec.setVirtualAddress(address);
    variables.createAlias(address.getStruct(), memory.getAddressArg().getName());

    data = new IntegerVariable(memory.getDataArg().getName(), memory.getDataArg().getBitSize());
    variables.defineVariable(data);

    for (final Variable variable : memory.getVariables()) {
      variables.declare(variable);
    }

    registerControlFlowForMemory(memory);

    System.out.println("---------------------------------");
    System.out.println(spec);
  }

  private void registerAction(final MmuAction action) {
    if (null != currentMarks) {
      for (final String mark : currentMarks) {
        action.addMark(mark);
      }
      currentMarks = null;
    }

    spec.registerAction(action);
  }

  private void registerMark(final StmtMark stmt) {
    if (null == currentMarks) {
      currentMarks = new ArrayList<>();
    }
    currentMarks.add(stmt.getName());
  }

  private MmuAction newBranch(final String text) {
    return new MmuAction(String.format("Branch_%d[%s]", actionIndex++, text));
  }

  private MmuAction newJoin() {
    return new MmuAction(String.format("Join_%d", actionIndex++));
  }

  private void registerAddress(final Address address) {
    final Variable struct = new Variable(address.getId(), address.getContentType());
    variables.declare(struct);

    final IntegerVariable addressVar =
        variables.get(struct.accessNested(address.getAccessChain()));

    spec.registerAddress(new MmuAddressType(struct, addressVar));
  }

  private void registerDevice(final Buffer buffer) {
    final MmuAddressType address = spec.getAddress(buffer.getAddress().getId());
    final boolean isReplaceable = PolicyId.NONE != buffer.getPolicy();

    final String addressArgName = buffer.getAddressArg().getName();
    final Variable addressVar = address.getStruct();

    // variables.defineGroupAs(address.getStruct(), addressArgName);
    final Variable bufferVar = new Variable(buffer.getId(), buffer.getEntry());
    variables.declareGlobal(bufferVar);
    variables.createAlias(addressVar, addressArgName);
    for (final Map.Entry<String, Variable> entry : addressVar.getFields().entrySet()) {
      variables.createAlias(entry.getValue(), entry.getKey());
    }

    try {
      final AddressFormatExtractor addressFormat = new AddressFormatExtractor(
          variables, address.getVariable(), buffer.getIndex(), buffer.getMatch());

      final MmuBuffer parentDevice = (null != buffer.getParent()) ?
          spec.getBuffer(buffer.getParent().getId()) : null;

      final MmuCondition guardCondition;
      if (buffer.getGuard() != null) {
        final GuardExtractor guardExtractor = 
            new GuardExtractor(spec, new AtomExtractor("", variables), buffer.getGuard());
        guardCondition = guardExtractor.getGuard().getCondition();
      } else {
        guardCondition = null;
      }

      final MmuBuffer device = new MmuBuffer(
          buffer.getId(),
          buffer.getWays().longValue(),
          buffer.getSets().longValue(),
          address,
          addressFormat.getTagExpr(),
          addressFormat.getIndexExpr(),
          addressFormat.getOffsetExpr(),
          guardCondition,
          null, // TODO: Guard
          isReplaceable,
          parentDevice
          );

      final Type entryType = buffer.getEntry();
      for (final Map.Entry<String, Type> entry : entryType.getFields().entrySet()) {
        final IntegerVariable fieldVar =
            new IntegerVariable(entry.getKey(), entry.getValue().getBitSize());
        device.addField(fieldVar);
      }

      context.registerBuffer(bufferVar, device);
      spec.registerBuffer(device);
    } finally {
      variables.removeAlias(addressArgName);
      for (final String fieldAlias : addressVar.getFields().keySet()) {
        variables.removeAlias(fieldAlias);
      }
    }
  }

  private void registerControlFlowForMemory(final Memory memory) {
    final MmuAddressType address = spec.getAddress(memory.getAddress().getId());

    final MmuAction root = new MmuAction("ROOT", new MmuAssignment(address.getVariable()));
    registerAction(root);
    spec.setStartAction(root);

    final MmuAction start = new MmuAction("START");
    registerAction(start);

    // The control flow graph terminates in the STOP node if no exceptions are raised.
    registerAction(STOP);

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

    currentMarks = null;
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
          
        case MARK:
          registerMark((StmtMark) stmt);
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

    if (isAddressTranslation(right)) {
      return registerCall(source, left, (AttributeRef) right.getUserData());
    }

    final AtomExtractor atomExtractor = context.newAtomExtractor();

    final Atom lhs = atomExtractor.extract(left);
    if (Atom.Kind.VARIABLE != lhs.getKind() && 
        Atom.Kind.GROUP != lhs.getKind() &&
        Atom.Kind.FIELD != lhs.getKind()) {
      throw new IllegalArgumentException(left + " cannot be used as left side of assignment.");
    }

    Atom rhs = atomExtractor.extract(right);
    if (Atom.Kind.VALUE == rhs.getKind()) {
      final BigInteger value = (BigInteger) rhs.getObject();
      rhs = Atom.newConcat(MmuExpression.val(value, lhs.getWidth()));
    }

    final String name = String.format("Assignment (%s = %s)", left, right);
    final AssignmentBuilder assignmentBuilder =
        new AssignmentBuilder(name, lhs, rhs, context);

    final MmuAction target = assignmentBuilder.build();
    registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);

    return target;
  }

  private MmuAction registerCall(final MmuAction source, final Node lhs, final AttributeRef rhs) {
    final AbstractStorage storage = rhs.getTarget();

    final String prefix = context.getPrefix(storage.getId());
    for (final Variable var : storage.getVariables()) {
      defineNestedVar(prefix, var);
    }

    final Variable input = defineNestedVar(prefix, storage.getAddressArg());
    final Variable output = defineNestedVar(prefix, storage.getDataArg());

    final MmuAction preAction =
        registerAssignment(source, input.getNode(), rhs.getAddressArgValue());

    context.pushPrefix(prefix);
    final MmuAction midAction =
        registerControlFlow(preAction, rhs.getAttribute().getStmts());
    context.popPrefix();

    return registerAssignment(midAction, lhs, output.getNode());
  }

  private MmuAction registerAssignment(final MmuAction source, final Node lhs, final Node rhs) {
    return registerAssignment(source, new StmtAssign(lhs, rhs));
  }

  private Variable defineNestedVar(final String prefix, final Variable var) {
    final Variable nested = var.rename(dotConc(prefix, var.getName()));
    variables.declare(nested);
    return nested;
  }

  private boolean isDataVariable(Node expr) {
    if (expr.getKind() != Node.Kind.VARIABLE) {
      return false;
    }

    final String name = ((NodeVariable) expr).getName();
    final IntegerVariable variable = variables.getVariable(name);

    return data.equals(variable);
  }

  private static boolean isAddressTranslation(final Node e) {
    if (e.getUserData() instanceof AttributeRef) {
      final AttributeRef ref = (AttributeRef) e.getUserData();
      return ref.getTarget() instanceof Segment;
    }
    return false;
  }

  private static boolean isStruct(final Node node) {
    if (node.getUserData() instanceof Variable) {
      return ((Variable) node.getUserData()).isStruct();
    }
    return false;
  }

  private MmuAction registerIf(final MmuAction source, final StmtIf stmt) {
    final MmuAction join = newJoin();
    registerAction(join);

    MmuAction current = source;

    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node condition = block.first;
      final List<Stmt> stmts = block.second;

      final GuardExtractor guardExtractor = 
          new GuardExtractor(spec, context.newAtomExtractor(), condition);

      final MmuAction ifTrueStart = newBranch(condition.toString());
      registerAction(ifTrueStart);

      final MmuGuard guardIfTrue = guardExtractor.getGuard();
      spec.registerTransition(new MmuTransition(current, ifTrueStart, guardIfTrue));

      final MmuAction ifTrueStop = registerControlFlow(ifTrueStart, stmts);
      if (null != ifTrueStop) {
        spec.registerTransition(new MmuTransition(ifTrueStop, join));
      }

      final MmuAction ifFalseStart = newBranch("not " + condition.toString());
      registerAction(ifFalseStart);

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
    registerAction(target);

    final MmuTransition transition = new MmuTransition(source, target);
    spec.registerTransition(transition);
  }
}
