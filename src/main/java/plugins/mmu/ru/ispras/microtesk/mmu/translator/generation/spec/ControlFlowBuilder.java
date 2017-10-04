/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.translator.MmuSymbolKind;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssert;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtCall;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtReturn;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;

final class ControlFlowBuilder {
  public static final Class<?> BUFFER_EVENT_CLASS =
      ru.ispras.microtesk.mmu.basis.BufferAccessEvent.class;

  public static final Class<?> ACTION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction.class;

  public static final Class<?> BINDING_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding.class;

  public static final Class<?> BUFFER_ACCESS_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess.class;

  public static final Class<?> GUARD_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard.class;

  public static final Class<?> SEGMENT_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment.class;

  public static final Class<?> TRANSITION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition.class;

  private final Ir ir;
  private final String context;

  private final ST st;
  private final STGroup group;
  private final ST stDef;

  private final Set<String> exceptions = new HashSet<>();
  private List<String> currentMarks = null;

  private int branchIndex = 0;
  private int joinIndex = 0;
  private int assignIndex = 0;
  private int guardIndex = 0;
  private int callIndex = 0;
  private int temporaryIndex = 0;

  protected ControlFlowBuilder(
      final Ir ir,
      final String context,
      final ST st,
      final STGroup group,
      final ST stDef) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(st);
    InvariantChecks.checkNotNull(group);
    InvariantChecks.checkNotNull(stDef);

    this.ir = ir;
    this.context = context;
    this.st = st;
    this.group = group;
    this.stDef = stDef;
  }

  protected static void buildImports(final ST st, final STGroup group) {
    InvariantChecks.checkNotNull(st);
    InvariantChecks.checkNotNull(group);

    st.add("imps", Arrays.class.getName());
    st.add("imps", BigInteger.class.getName());
    st.add("imps", BUFFER_EVENT_CLASS.getName());
    st.add("imps", ACTION_CLASS.getName());
    st.add("imps", BINDING_CLASS.getName());
    st.add("imps", BUFFER_ACCESS_CLASS.getName());
    st.add("imps", GUARD_CLASS.getName());
    st.add("imps", SEGMENT_CLASS.getName());
    st.add("imps", TRANSITION_CLASS.getName());
  }

  private String newBranch() {
    return String.format("BRANCH_%d", branchIndex++);
  }

  private String newJoin() {
    return String.format("JOIN_%d", joinIndex++);
  }

  private String newAssign() {
    return String.format("ASSIGN_%d", assignIndex++);
  }

  private String newGuard() {
    return String.format("GUARD_%d", guardIndex++);
  }

  public void build(
      final String start,
      final String stop,
      final String startRead,
      final List<Stmt> stmtsRead,
      final String startWrite,
      final List<Stmt> stmtsWrite) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(stop);
    InvariantChecks.checkNotNull(startRead);
    InvariantChecks.checkNotNull(startWrite);
    InvariantChecks.checkNotNull(stmtsRead);
    InvariantChecks.checkNotNull(stmtsWrite);

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(start, true);
    buildAction(stop, true);

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(startRead);
    buildTransition(start, startRead, "new MmuGuard(MemoryOperation.LOAD)");
    final String stopRead = buildStmts(startRead, stop, stmtsRead);
    if (null != stopRead) {
      buildTransition(stopRead, stop);
    }

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(startWrite);
    buildTransition(start, startWrite, "new MmuGuard(MemoryOperation.STORE)");
    final String stopWrite = buildStmts(startWrite, stop, stmtsWrite);
    if (null != stopWrite) {
      buildTransition(stopWrite, stop);
    }
  }

  public void build(
      final String start,
      final String stop,
      final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(stop);
    InvariantChecks.checkNotNull(stmts);

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(start, true);
    buildAction(stop, true);

    st.add("members", "");
    stDef.add("stmts", "");

    final String current = buildStmts(start, stop, stmts);
    if (null != current) {
      buildTransition(current, stop);
    }
  }

  public void build(
      final String start,
      final String stop,
      final Var retval,
      final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    InvariantChecks.checkNotNull(stop);
    InvariantChecks.checkNotNull(stmts);

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(start, true);
    buildAction(stop, true);

    st.add("members", "");
    stDef.add("stmts", "");

    final String current = buildStmts(start, stop, stmts);
    if (null != current) {
      buildTransition(current, stop);
    }
  }

  private String buildStmts(final String start, final String stop, final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    String current = start;

    for (final Stmt stmt : stmts) {
      switch(stmt.getKind()) {
        case ASSIGN:
          current = buildStmtAssign(current, (StmtAssign) stmt);
          break;

        case IF:
          current = buildStmtIf(current, stop, (StmtIf) stmt);
          break;

        case ASSERT:
          current = buildStmtAssert(current, (StmtAssert) stmt);
          break;

        case EXCEPT:
          buildStmtException(current, (StmtException) stmt);
          // If current became null as a result of exception,
          // other statements will not be traversed.
          return null;

        case MARK:
          buildStmtMark((StmtMark) stmt);
          break;

        case TRACE: // Ignored
          break;

        case RETURN:
          buildStmtReturn(current, stop, (StmtReturn) stmt);
          return null;

        case CALL:
          current = buildStmtCall(current, (StmtCall) stmt);
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }
    }

    return current;
  }

  private String buildStmtAssign(final String source, final StmtAssign stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    return buildStmtAssign(source, stmt.getLeft(), stmt.getRight());
  }

  private String buildStmtAssign(final String source, final Node left, final Node right) {
    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

    // Reading from a segment (address translation) is performed
    // by connecting to a control flow graph of a corresponding segment.
    if (isSegmentAccess(right)) {
      return buildSegmentAccess(source, left, (AttributeRef) right.getUserData());
    }

    final CallBuilder rule = new CallBuilder(source);
    final Node value = Transformer.transform(right, MmuSymbolKind.FUNCTION, rule);

    final Atom lhs = AtomExtractor.extract(left);
    final Atom rhs = AtomExtractor.extract(value);

    if (Atom.Kind.VARIABLE != lhs.getKind() && 
        Atom.Kind.GROUP != lhs.getKind() &&
        Atom.Kind.FIELD != lhs.getKind()) {
      throw new IllegalArgumentException(left + " cannot be used as left side of assignment.");
    }

    final String address = value.getUserData() instanceof AttributeRef ?
        getVariableName(((AttributeRef) value.getUserData()).getAddressArgValue().toString()) : null;

    final String target = newAssign();
    final String targetBindings = buildBindings(lhs, rhs);
    final String writeAccess = selectBufferAccess(BufferAccessEvent.WRITE, address, lhs);
    final String readAccess = selectBufferAccess(BufferAccessEvent.READ, address, rhs);

    if (writeAccess != null && readAccess != null) {
      throw new IllegalArgumentException("Both " + left + " and " + right + " access buffers.");
    }

    final String access = writeAccess != null ? writeAccess : readAccess;

    buildAction(target, filterEmpty(access, targetBindings));
    buildTransition(rule.getState(), target);

    return target;
  }

  private static String[] filterEmpty(final String... strings) {
    final List<String> list = new ArrayList<>(strings.length);
    for (final String s : strings) {
      if (s != null && !s.isEmpty()) {
        list.add(s);
      }
    }
    return list.toArray(new String[list.size()]);
  }

  private String buildStmtAssert(final String source, final StmtAssert stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    final GuardPrinter guardPrinter =
        new GuardPrinter(ir, context, stmt.getCondition());

    final String target = newGuard();

    buildAction(target);
    buildTransition(source, target, guardPrinter.getGuard());

    return target;
  }

  private String buildStmtIf(final String source, final String stop, final StmtIf stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    String join = null;
    List<Stmt> elseBlock = stmt.getElseBlock();

    final List<Node> elseConditions = new ArrayList<>();
    Node jointElseCondition = null;

    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node condition = block.first;
      final List<Stmt> stmts = block.second;

      if (ExprUtils.isValue(condition)) {
        final boolean isCondition = ((NodeValue) condition).getBoolean();
        if (isCondition) {
          // If condition is true, the current block is visited unconditionally (treated as else)
          // and all other subsequent blocks are ignored (as never reached).
          elseBlock = stmts;
          break;
        } else {
          // If condition is false, the current block is ignored (as never reached).
          continue;
        }
      }

      final String branchStart = buildBranch(source, condition);
      final String branchStop = buildStmts(branchStart, stop, stmts);

      if (null != branchStop) {
        if (null == join) {
          join = newJoin();
          buildAction(join);
        }

        buildTransition(branchStop, join);
      }

      final Node elseCondition = new NodeOperation(StandardOperation.NOT, condition);
      if (ExprUtils.isOperation(condition, StandardOperation.EQ) ||
          ExprUtils.isOperation(condition, StandardOperation.NOTEQ)) {
        jointElseCondition = null == jointElseCondition ?
            elseCondition :
            new NodeOperation(StandardOperation.AND, jointElseCondition, elseCondition);
      } else {
        elseConditions.add(elseCondition);
      }
    }

    if (null != jointElseCondition) {
      elseConditions.add(jointElseCondition);
    }

    String elseBranchStart = source;
    for (final Node elseCondition : elseConditions) {
      elseBranchStart = buildBranch(elseBranchStart, elseCondition);
    }

    final String elseBranchStop = buildStmts(elseBranchStart, stop, elseBlock);
    if (null != elseBranchStop) {
      // If all other condition branches ended with null (exception)
      // and action 'join' for merging branches was not created,
      // we do not need it since there is only one branch (else).
      if (null == join) {
        join = elseBranchStop;
      } else {
        buildTransition(elseBranchStop, join);
      }
    }

    return join;
  }

  private String buildBranch(final String source, final Node condition) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(condition);

    final GuardPrinter guardPrinter = new GuardPrinter(ir, context, condition);
    final String branchStart = newBranch();

    buildAction(branchStart);
    buildTransition(source, branchStart, guardPrinter.getGuard());

    return branchStart;
  }

  private void buildStmtException(final String source, final StmtException stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    final String exception = stmt.getMessage();
    if (!exceptions.contains(exception)) {
      buildAction(exception, Boolean.toString(true)); // isException=true
      exceptions.add(exception);
    }

    buildTransition(source, exception);
  }

  private void buildStmtMark(final StmtMark stmt) {
    InvariantChecks.checkNotNull(stmt);

    if (null == currentMarks) {
      currentMarks = new ArrayList<>();
    }
    currentMarks.add(stmt.getName());
  }

  private void buildStmtReturn(final String start, final String stop, final StmtReturn s) {
    final String medium;
    if (s.getStorage() != null) {
      medium = buildStmtAssign(start, s.getStorage().getNode(), s.getExpr());
    } else {
      medium = start;
    }
    buildTransition(medium, stop);
  }

  private String buildStmtCall(final String start, final StmtCall s) {
    final CallBuilder rule = new CallBuilder(start);
    final List<Node> args =
        Transformer.transformAll(s.getArguments(), MmuSymbolKind.FUNCTION, rule);

    final Node lhs;
    final Callable callee = s.getCallee();
    if (callee.getOutput() != null) {
      lhs = newTemporary(callee.getOutput().getType()).getNode();
    } else {
      lhs = null;
    }
    return buildCall(rule.getState(), lhs, callee, args);
  }

  private void buildAction(final String id, final String... args) {
    buildAction(id, false, args);
  }

  private void buildAction(final String id, boolean isPublic, final String... args) {
    InvariantChecks.checkNotNull(id);

    final ST stActionDecl = group.getInstanceOf("action_decl");
    stActionDecl.add("id", id);
    stActionDecl.add("is_public", isPublic);
    st.add("members", stActionDecl);

    final ST stActionDef = group.getInstanceOf("action_def");
    stActionDef.add("id", id);
    stActionDef.add("name", id);

    for (final String arg : args) {
      stActionDef.add("args", arg);
    }

    if (null != currentMarks) {
      for (final String mark : currentMarks) {
        stActionDef.add("marks", mark);
      }
      currentMarks = null;
    }

    stDef.add("stmts", stActionDef);
    stDef.add("stmts", String.format("builder.registerAction(%s);", id));
  }

  private void buildTransition(final String source, final String target) {
    buildTransition(source, target, null);
  }

  private void buildTransition(final String source, final String target, final String guard) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(target);

    final ST stTrans = group.getInstanceOf("transition");

    stTrans.add("source", source);
    stTrans.add("target", target);

    if (null != guard) {
      stTrans.add("guard", guard);
    }

    stDef.add("stmts", stTrans);
  }

  private String selectBufferAccess(
      final BufferAccessEvent event,
      final String address,
      final Atom... atoms) {
    for (final Atom atom : atoms) {
      if (atom.getKind().isStruct()) {
        final String name = ((Var) atom.getObject()).getName();
        if (isBufferAccess(name)) {
          return defaultBufferAccess(name, event, address);
        }
      }
    }
    return null;
  }

  protected static String defaultBufferAccess(
      final String name,
      final BufferAccessEvent event,
      final String address) {
    return String.format(
        "new MmuBufferAccess(%s.get(), BufferAccessEvent.%s, %s.get().getAddress(), %s.get(), %s)",
        name,
        event,
        name,
        name,
        address
        );
  }

  private boolean isBufferAccess(final String variableName) {
    return ir.getBuffers().containsKey(variableName);
  }

  private boolean isSegmentAccess(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (expr.getUserData() instanceof AttributeRef) {
      final AttributeRef ref = (AttributeRef) expr.getUserData();
      return ref.getTarget() instanceof Segment;
    }
    return false;
  }

  private String buildSegmentAccess(final String source, final Node left, final AttributeRef right) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(left);
    InvariantChecks.checkNotNull(right);

    final String callId = String.format("call_%d", callIndex);
    final String callText = String.format(
        "%s.Function %s = %s.get().newCall(builder, %s, %s);",
        right.getTarget().getId(),
        callId,
        right.getTarget().getId(),
        getVariableName(right.getAddressArgValue().toString()),
        getVariableName(left.toString())
        );
 
    stDef.add("stmts", callText);

    final String segmentStart = String.format("%s.START", callId, right.getTarget().getId());
    final String segmentStop = String.format("%s.STOP", callId, right.getTarget().getId());
    buildTransition(source, segmentStart);

    /*
    final Atom lhs = AtomExtractor.extract(left);
    final Atom rhs = AtomExtractor.extract(right.getTarget().getDataArg().getNode());
 
    final String assignResult = newAssign();
    final String assignResultBindings = buildBindings(lhs, rhs);

    buildAction(assignResult, assignResultBindings);
    buildTransition(segmentStop, assignResult);*/

    callIndex++;
    //return assignResult;
    return segmentStop;
  }

  private Var newTemporary(final Type type) {
    final String name = String.format("TMP_%d", temporaryIndex++);

    final ST temporary = group.getInstanceOf("temporary");
    temporary.add("name", name);
    if (type.isStruct()) {
      temporary.add("type", type.getId());
    } else {
      temporary.add("type", "IntegerVariable");
      temporary.add("size", String.valueOf(type.getBitSize()));
    }
    stDef.add("stmts", temporary);

    return new Var(context + "." + name, type);
  }

  private String buildCall(
      final String source,
      final Node lhs,
      final Callable callee,
      final List<Node> args) {
    checkNotNull(source);
    checkNotNull(callee);
    checkNotNull(args);
    checkTrue((lhs == null) == (callee.getOutput() == null));
    checkTrue(callee.getParameters().size() == args.size());

    final StringBuilder builder = new StringBuilder();

    final String callId = String.format("call_%d", callIndex++);
    builder.append(String.format(
        "%s.Function %s = %s.get().newCall(builder",
        callee.getName(),
        callId,
        callee.getName()));
    if (lhs != null) {
      builder.append(", ");
      builder.append(getVariableName(lhs.toString()));
    }
    for (int i = 0; i < args.size(); ++i) {
      final Node arg = args.get(i);

      builder.append(", ");
      switch (arg.getKind()) {
      case VARIABLE:
        builder.append(getVariableName(arg.toString()));
        break;

      case VALUE:
        builder.append(integerLiteral(callee.getParameter(i).getType(), (NodeValue) arg));
        break;

      default:
        checkTrue(false);
      };
    }
    builder.append(");");
 
    stDef.add("stmts", builder.toString());

    final String funcStart = callId + ".START";
    final String funcStop = callId + ".STOP";
    buildTransition(source, funcStart);

    return funcStop;
  }

  private static String integerLiteral(final Type type, final NodeValue node) {
    final Atom value = AtomExtractor.extract(node);
    return String.format(
        "new IntegerVariable(%d, %s)",
        type.getBitSize(),
        Utils.toString((BigInteger) value.getObject()));
  }

  private final class CallBuilder implements TransformerRule {
    private String state;
    
    public CallBuilder(final String state) {
      this.state = state;
    }

    @Override
    public boolean isApplicable(final Node node) {
      final boolean process = nodeIsOperation(node, MmuSymbolKind.FUNCTION) &&
             node.getUserData() instanceof Callable;
      return process;
    }

    @Override
    public Node apply(final Node node) {
      final NodeOperation call = (NodeOperation) node;
      final Callable callee = (Callable) node.getUserData();

      int i = 0;
      final List<Node> args = new ArrayList<>(call.getOperandCount());
      for (final Node arg : call.getOperands()) {
        // In bottom-up transformer every function call has been replaced earlier
        if (arg.getKind() == Node.Kind.OPERATION) {
          final NodeOperation e = (NodeOperation) arg;
          final Var tmp = newTemporary(callee.getParameter(i).getType());
          state = buildStmtAssign(state, tmp.getNode(), e);
          args.add(tmp.getNode());
        } else {
          args.add(arg);
        }
        ++i;
      }
      final Var tmp = newTemporary(callee.getOutput().getType());
      state = buildCall(state, tmp.getNode(), callee, args);

      return tmp.getNode();
    }
   
    public String getState() {
      return state;
    }
  }

  private static boolean nodeIsOperation(final Node node, final Enum<?> op) {
    checkNotNull(node);
    checkNotNull(op);

    return node.getKind() == Node.Kind.OPERATION &&
           ((NodeOperation) node).getOperationId().equals(op);
  }

  private String buildBindings(final Atom lhs, final Atom rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    if (lhs.getKind().isStruct() && rhs.getKind().isStruct()) {
      final Var left = (Var) lhs.getObject();
      final Var right = (Var) rhs.getObject();

      if (!left.getType().equals(right.getType())) {
        throw new IllegalArgumentException(String.format("Type mismatch: %s = %s", left, right));
      }

      if (isBufferAccess(left.getName())) {
        return "";
      }

      return String.format("%s, %s", toString(lhs), toString(rhs));
    } else if (lhs.getKind().isStruct()) {
      final Var left = (Var) lhs.getObject();
      InvariantChecks.checkTrue(left.getFields().size() == 1);

      final Var leftField = left.getFields().values().iterator().next();
      InvariantChecks.checkFalse(leftField.isStruct());

      final Atom lhsField = AtomExtractor.extract(leftField.getNode());
      return String.format("new MmuBinding(%s, %s)",
          toString(lhsField),
          toString(rhs)
          );
    } else if (rhs.getKind().isStruct()) {
      final Var right = (Var) rhs.getObject();
      InvariantChecks.checkTrue(right.getFields().size() == 1);

      final Var rightField = right.getFields().values().iterator().next();
      InvariantChecks.checkFalse(rightField.isStruct());

      final Atom rhsField = AtomExtractor.extract(rightField.getNode());
      return String.format(
          "new MmuBinding(%s, %s)",
          toString(lhs),
          toString(rhsField)
          );
    } else {
      return String.format("new MmuBinding(%s, %s)", toString(lhs), toString(rhs));
    }
  }

  private String toString(final Atom atom) {
    return Utils.toString(ir, context, atom);
  }

  private String getVariableName(final String name) {
    return Utils.getVariableName(context, name);
  }
}
