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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.transformer.Transformer;
import ru.ispras.fortress.transformer.TransformerRule;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.MmuSymbolKind;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtCall;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtReturn;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkTrue;

final class ControlFlowBuilder {
  public static final Class<?> BUFFER_EVENT_CLASS =
      ru.ispras.microtesk.mmu.basis.BufferAccessEvent.class;

  public static final Class<?> ACTION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuAction.class;

  public static final Class<?> BINDING_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuBinding.class;

  public static final Class<?> COND_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition.class;

  public static final Class<?> COND_ATOM_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom.class;

  public static final Class<?> EXPRESSION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression.class;

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
    st.add("imps", COND_CLASS.getName());
    st.add("imps", COND_ATOM_CLASS.getName());
    st.add("imps", EXPRESSION_CLASS.getName());
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
      final Variable retval,
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
          current = buildStmtCall(start, (StmtCall) stmt);
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

    // Assignments that use the "data" MMU variable are ignored.
    if (isDataVariable(left) || isDataVariable(right)) {
      return source;
    }

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

    final String target = newAssign();
    final String targetBindings = buildBindings(lhs, rhs);

    buildAction(target, targetBindings);
    buildTransition(rule.getState(), target);

    return target;
  }

  private String buildStmtIf(final String source, final String stop, final StmtIf stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    String current = source;
    String join = null;

    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node condition = block.first;
      final List<Stmt> stmts = block.second;

      if (condition.getKind() == Node.Kind.VALUE) {
        final boolean isCondition = ((NodeValue) condition).getBoolean();

        if (isCondition) {
          // If condition is true, the current block is visited unconditionally
          // and all other subsequent blocks are ignored (as never reached).
          current = buildStmts(current, stop, stmts);

          if (null != current) {
            // If all other condition branches ended with null (exception)
            // and action 'join' for merging branches was not created,
            // we do not need it since there is only one branch (else).
            if (null == join) {
              join = current;
            } else {
              buildTransition(current, join);
            }
          }

          return join;
        } else {
          // If condition is false, the current block is ignored (as never reached).
          continue;
        }
      }

      final GuardPrinter guardPrinter = 
          new GuardPrinter(ir, context, condition);

      final String ifTrueStart = newBranch();
      buildAction(ifTrueStart);

      buildTransition(current, ifTrueStart, guardPrinter.getGuard());

      final String ifTrueStop = buildStmts(ifTrueStart, stop, stmts);
      if (null != ifTrueStop) {
        if (null == join) {
          join = newJoin();
          buildAction(join);
        }

        buildTransition(ifTrueStop, join);
      }

      final String ifFalseStart = newBranch();
      buildAction(ifFalseStart);

      buildTransition(current, ifFalseStart, guardPrinter.getNegatedGuard());
      current = ifFalseStart;
    }

    current = buildStmts(current, stop, stmt.getElseBlock());
    if (null != current) {
      // If all other condition branches ended with null (exception)
      // and action 'join' for merging branches was not created,
      // we do not need it since there is only one branch (else).
      if (null == join) {
        join = current;
      } else {
        buildTransition(current, join);
      }
    }

    return join;
  }

  private void buildStmtException(final String source, final StmtException stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    final String exception = stmt.getMessage();
    if (!exceptions.contains(exception)) {
      buildAction(exception);
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

  private boolean isDataVariable(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    final Memory memory = ir.getMemories().get(context);
    if (null == memory) {
      return false;
    }

    if (expr.getKind() != Node.Kind.VARIABLE) {
      return false;
    }

    if (!(expr.getUserData() instanceof Variable)) {
      return false;
    }
    return memory.getDataArg().equals(expr.getUserData());
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

  private Variable newTemporary(final Type type) {
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

    return new Variable(context + "." + name, type);
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
          final Variable tmp = newTemporary(callee.getParameter(i).getType());
          state = buildStmtAssign(state, tmp.getNode(), e);
          args.add(tmp.getNode());
        } else {
          args.add(arg);
        }
        ++i;
      }
      final Variable tmp = newTemporary(callee.getOutput().getType());
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
      final Variable left = (Variable) lhs.getObject();
      final Variable right = (Variable) rhs.getObject();

      if (!left.getType().equals(right.getType())) {
        throw new IllegalArgumentException(String.format("Type mismatch: %s = %s", left, right));
      }

      if (isBufferAccess(left.getName())) {
        return String.format("%s /* no bindings for write */", toString(lhs));
      }

      return String.format("%s, %s", toString(lhs), toString(rhs));
    } else if (lhs.getKind().isStruct()) {
      final Variable left = (Variable) lhs.getObject();
      InvariantChecks.checkTrue(left.getFields().size() == 1);

      final Variable leftField = left.getFields().values().iterator().next();
      InvariantChecks.checkFalse(leftField.isStruct());

      final Atom lhsField = AtomExtractor.extract(leftField.getNode());
      return String.format("%snew MmuBinding(%s, %s)",
          isBufferAccess(left.getName()) ? toString(lhs) + ", " : "",
          toString(lhsField),
          toString(rhs)
          );
    } else if (rhs.getKind().isStruct()) {
      final Variable right = (Variable) rhs.getObject();
      InvariantChecks.checkTrue(right.getFields().size() == 1);

      final Variable rightField = right.getFields().values().iterator().next();
      InvariantChecks.checkFalse(rightField.isStruct());

      final Atom rhsField = AtomExtractor.extract(rightField.getNode());
      return String.format(
          "%snew MmuBinding(%s, %s)",
          isBufferAccess(right.getName()) ? toString(rhs) + ", " : "",
          toString(lhs),
          toString(rhsField)
          );
    } else {
      return String.format("new MmuBinding(%s, %s)", toString(lhs), toString(rhs));
    }
  }

  @SuppressWarnings("unchecked")
  private String toString(final Atom atom) {
    InvariantChecks.checkNotNull(atom);

    final Object object = atom.getObject();
    switch (atom.getKind()) {
      case VALUE: {
        return Utils.toString((BigInteger) object);
      }

      case VARIABLE: {
        final IntegerVariable variable = (IntegerVariable) object;
        return getVariableName(variable);
      }

      case FIELD: {
        final IntegerField field = (IntegerField) object;
        final IntegerVariable variable = field.getVariable();
        return String.format("%s.field(%d, %d)",
            getVariableName(variable),
            field.getLoIndex(),
            field.getHiIndex()
            );
      }

      case GROUP: {
        return getVariableName( ((Variable) object).getName());
      }

      case CONCAT: {
        return Utils.toMmuExpressionText(context, (List<IntegerField>) object);
      }

      default:
        throw new IllegalStateException("Unsupported atom kind: " + atom.getKind());
    }
  }

  private String getVariableName(final String name) {
    return Utils.getVariableName(context, name);
  }

  private String getVariableName(final IntegerVariable variable) {
    final String name = variable.getName();
    final Constant constant = ir.getConstants().get(name);

    if (null != constant) {
      final DataType type = constant.getVariable().getDataType();
      if (variable.getWidth() == type.getSize()) {
        return name + ".get()";
      } else {
        return String.format("%s.get(%d)", name, variable.getWidth());
      }
    }

    return Utils.getVariableName(context, name);
  }
}
