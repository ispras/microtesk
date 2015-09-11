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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

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
    final String stopRead = buildStmts(startRead, stmtsRead);
    if (null != stopRead) {
      buildTransition(stopRead, stop);
    }

    st.add("members", "");
    stDef.add("stmts", "");

    buildAction(startWrite);
    buildTransition(start, startWrite, "new MmuGuard(MemoryOperation.STORE)");
    final String stopWrite = buildStmts(startWrite, stmtsWrite);
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

    final String current = buildStmts(start, stmts);
    if (null != current) {
      buildTransition(current, stop);
    }
  }

  private String buildStmts(final String start, final List<Stmt> stmts) {
    InvariantChecks.checkNotNull(start);
    String current = start;

    for (final Stmt stmt : stmts) {
      switch(stmt.getKind()) {
        case ASSIGN:
          current = buildStmtAssign(current, (StmtAssign) stmt);
          break;

        case IF:
          current = buildStmtIf(current, (StmtIf) stmt);
          break;

        case EXCEPT:
          buildStmtException(current, (StmtException) stmt);
          current = null;
          break;

        case MARK:
          buildStmtMark((StmtMark) stmt);
          break;

        case TRACE: // Ignored
          break;

        default:
          throw new IllegalStateException("Unknown statement: " + stmt.getKind());
      }

      // If current became null as a result of exception,
      // other statements will not be traversed.
      if (null == current) {
        break;
      }
    }

    return current;
  }

  private String buildStmtAssign(final String source, final StmtAssign stmt) {
    InvariantChecks.checkNotNull(source);
    InvariantChecks.checkNotNull(stmt);

    final Node left = stmt.getLeft();
    final Node right = stmt.getRight();

    // Assignments that use the "data" MMU variable are ignored.
    if (isDataVariable(left) || isDataVariable(right)) {
      return source;
    }

    // Reading from a segment (address translation) is performed
    // by connecting to a control flow graph of a corresponding segment.
    if (isSegmentAccess(right)) {
      return buildSegmentAccess(source, left, (AttributeRef) right.getUserData());
    }

    final Atom lhs = AtomExtractor.extract(left);
    final Atom rhs = AtomExtractor.extract(right);

    if (Atom.Kind.VARIABLE != lhs.getKind() && 
        Atom.Kind.GROUP != lhs.getKind() &&
        Atom.Kind.FIELD != lhs.getKind()) {
      throw new IllegalArgumentException(left + " cannot be used as left side of assignment.");
    }

    final String target = newAssign();
    final String targetBindings = buildBindings(lhs, rhs);

    buildAction(target, targetBindings);
    buildTransition(source, target);

    return target;
  }

  private String buildStmtIf(final String source, final StmtIf stmt) {
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
          current = buildStmts(current, stmts);

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

      final String ifTrueStop = buildStmts(ifTrueStart, stmts);
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

    current = buildStmts(current, stmt.getElseBlock());
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

    final Variable variable = (Variable) expr.getUserData();
    return variable.equals(memory.getDataArg());
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
        Utils.getVariableName(context, right.getAddressArgValue().toString()),
        Utils.getVariableName(context, left.toString())
        );
 
    stDef.add("stmts", callText);

    final String segmentStart = String.format("%s.START", callId, right.getTarget().getId());
    final String segmentStop = String.format("%s.STOP", callId, right.getTarget().getId());
    buildTransition(source, segmentStart);

    final Atom lhs = AtomExtractor.extract(left);
    final Atom rhs = AtomExtractor.extract(right.getTarget().getDataArg().getNode());
 
    /*
    final String assignResult = newAssign();
    final String assignResultBindings = buildBindings(lhs, rhs);

    buildAction(assignResult, assignResultBindings);
    buildTransition(segmentStop, assignResult);*/

    callIndex++;
    //return assignResult;
    return segmentStop;
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

  private String toString(final Atom atom) {
    InvariantChecks.checkNotNull(atom);

    final Object object = atom.getObject();
    switch (atom.getKind()) {
      case VALUE:
        return Utils.toString((BigInteger) object);

      case VARIABLE:
        return Utils.getVariableName(context, ((IntegerVariable) object).getName());

      case FIELD:
        return String.format("%s.field(%d, %d)", 
            Utils.getVariableName(context, ((IntegerField) object).getVariable().getName()),
            ((IntegerField) object).getLoIndex(),
            ((IntegerField) object).getHiIndex());

      case GROUP:
        return Utils.getVariableName(context, ((Variable) object).getName());

      case CONCAT: {
        @SuppressWarnings("unchecked")
        final List<IntegerField> fields = (List<IntegerField>) object;
        final List<String> fieldTexts = new ArrayList<>();

        for (final IntegerField field : fields) {
          fieldTexts.add(Utils.toString(context, field));
        }

        return String.format(
            "MmuExpression.rcat(%s)", Utils.toString(fieldTexts, ", "));
      }

      default:
        throw new IllegalStateException("Unsupported atom kind: " + atom.getKind());
    }
  }
}
