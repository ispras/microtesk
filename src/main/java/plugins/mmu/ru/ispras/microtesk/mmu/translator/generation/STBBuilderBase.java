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

package ru.ispras.microtesk.mmu.translator.generation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

public abstract class STBBuilderBase {
  public static final Class<?> BIT_VECTOR_CLASS =
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> ADDRESS_CLASS =
      ru.ispras.microtesk.mmu.model.api.Address.class;

  public static final Class<?> DATA_CLASS =
      ru.ispras.microtesk.mmu.model.api.Data.class;

  public static final Class<?> BUFFER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Buffer.class;

  public static final Class<?> CACHE_CLASS =
      ru.ispras.microtesk.mmu.model.api.Cache.class;

  public static final Class<?> SEGMENT_CLASS =
      ru.ispras.microtesk.mmu.model.api.Segment.class;

  public static final Class<?> MEMORY_CLASS =
      ru.ispras.microtesk.mmu.model.api.Memory.class;

  public static final Class<?> INDEXER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Indexer.class;

  public static final Class<?> MATCHER_CLASS =
      ru.ispras.microtesk.mmu.model.api.Matcher.class;

  public static final Class<?> POLICY_ID_CLASS =
      ru.ispras.microtesk.mmu.model.api.PolicyId.class;

  private final String packageName;

  protected STBBuilderBase(final String packageName) {
    InvariantChecks.checkNotNull(packageName);
    this.packageName = packageName;
  }

  protected abstract String getId();

  protected final String removePrefix(final String name) {
    final String prefix = getId() + ".";
    InvariantChecks.checkTrue(name.startsWith(prefix), name + " prefix is expected: " + prefix);

    return name.substring(prefix.length());
  }

  protected final void buildHeader(final ST st, final String base) {
    st.add("name", getId()); 
    st.add("base", base);
    st.add("pack", packageName);

    st.add("imps", String.format("%s.*", BUFFER_CLASS.getPackage().getName()));
    st.add("imps", String.format("%s.*", BIT_VECTOR_CLASS.getPackage().getName()));
  }

  protected final void buildVariableDecl(final ST st, final Variable variable) {
    final String mappingName = removePrefix(variable.getName());

    final String typeName;
    if (variable.getTypeSource() instanceof Address) {
      typeName = ((Address) variable.getTypeSource()).getId();
    } else if (variable.getTypeSource() instanceof Buffer) {
      typeName = ((Buffer) variable.getTypeSource()).getId() + ".Entry";
    } else if (variable.isStruct()) {
      typeName = DATA_CLASS.getSimpleName();
    } else {
      typeName = BIT_VECTOR_CLASS.getSimpleName();
    }

    ExprPrinter.get().addVariableMappings(variable, mappingName);
    st.add("stmts", String.format("%s %s = null;", typeName,  mappingName));
  }

  protected final void buildVariableDecls(final ST st, final Collection<Variable> variables) {
    for (final Variable variable : variables) {
      buildVariableDecl(st, variable);
    }
    st.add("stmts", "");
  }

  protected final void buildStmts(final ST st, final STGroup group, final List<Stmt> stmts) {
    for (final Stmt stmt : stmts) {
      buildStmt(st, group, stmt);
    }
  }

  protected final void buildStmt(final ST st, final STGroup group, final Stmt stmt) {
    switch(stmt.getKind()) {
      case ASSIGN:
        buildStmtAssign(st, group, (StmtAssign) stmt);
        break;

      case IF:
        buildStmtIf(st, group, (StmtIf) stmt);
        break;

      case EXCEPT:
        buildStmtException(st, (StmtException) stmt);
        break;

      case MARK:
        buildStmtMark(st, (StmtMark) stmt);
        break;

      case TRACE:
        buildStmtTrace(st, (StmtTrace) stmt);
        break;

      default:
        throw new IllegalArgumentException(
            "Unsupported statement kind: " + stmt.getKind());
    }
  }

  private void buildStmtAssign(final ST st, final STGroup group, final StmtAssign stmt) {
    final Node right = correctRightType(stmt.getRight(), stmt.getLeft());
    final Node left = stmt.getLeft();

    // System.out.printf("!!! (1) %s = %s%n",left, right);
    // System.out.printf("!!! (2) %s = %s%n", left.getUserData(), right.getUserData());

    final String text;
    if (left.getKind() == Node.Kind.VARIABLE) {
      text = buildAssignment((NodeVariable) left, right);
    } else if (left.getKind() == Node.Kind.OPERATION){
      text = buildAssignment((NodeOperation) left, right);
    } else {
      throw new IllegalArgumentException(
          "Illegal left hand side expression kind: " + left.getKind());
    }

    if (null != text) {
      //st.add("stmts", text);
    }
  }

  private static Node correctRightType(final Node right, final Node left) {
    if (right.getKind() == Node.Kind.VALUE &&
        right.isType(DataTypeId.LOGIC_INTEGER) && 
        left.isType(DataTypeId.BIT_VECTOR)) {
      return NodeValue.newBitVector(BitVector.valueOf(
          ((NodeValue) right).getInteger(), left.getDataType().getSize()));
    }
    return right;
  }

  private String buildAssignment(final NodeVariable left, final Node right) {
    String text = null;
    final String rightText = ExprPrinter.get().toString(right);

    if (left.getUserData() instanceof Variable) {
      final Variable leftVar = (Variable) left.getUserData();
      if (leftVar.isField()) {
        // TODO
      } else {
        final String rightSuffix = leftVar.isStruct() ? "" : getRightFieldSuffix(right);
        final String leftText = ExprPrinter.get().toString(left);
        text = String.format("%s = %s%s;", leftText, rightText, rightSuffix);
      }
    } else if (left.getUserData() instanceof AttributeRef) {
      final AttributeRef leftAttrRef = (AttributeRef) left.getUserData();
      
    } else {
      throw new IllegalStateException(
          "Invalid left hand side expression: " + left.getUserData());
    }

    return text;
  }

  private String getRightFieldSuffix(final Node right) {
    if (right.getUserData() instanceof Variable) {
      final Variable rightVariable = (Variable) right.getUserData();
      final Map<String, Variable> fields = rightVariable.getFields();

      if (fields.size() == 1) {
        final Variable field = fields.values().iterator().next();
        final String prefix = rightVariable.getName() + ".";
        return String.format(".getField(\"%s\")", field.getName().replaceFirst(prefix, ""));
      }
    } else if (right.getUserData() instanceof AttributeRef) {
      final AttributeRef rightAttrRef = (AttributeRef) right.getUserData();
      final Variable data = rightAttrRef.getTarget().getDataArg();
      final Map<String, Variable> fields = data.getFields();

      if (fields.size() == 1) {
        final Variable field = fields.values().iterator().next();
        final String prefix = rightAttrRef.getTarget().getId() + ".";
        return String.format(".getField(\"%s\")", field.getName().replaceFirst(prefix, ""));
      }
    }

    return "";
  }

  private static String buildAssignment(final NodeOperation left, final Node right) {
    return null;
  }

  private void buildStmtIf(final ST st, final STGroup group, final StmtIf stmt) {
    boolean isFirst = true;
    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final ST stIf = group.getInstanceOf(isFirst ? "if_block" : "elseif_block");
      isFirst = false;

      final String exprText = ExprPrinter.get().toString(block.first);
      stIf.add("expr", exprText);

      buildStmts(stIf, group, block.second);
      st.add("stmts", stIf);
    }

    if (!stmt.getElseBlock().isEmpty()) {
      final ST stElse = group.getInstanceOf("else_block");
      buildStmts(stElse, group, stmt.getElseBlock());
      st.add("stmts", stElse);
    } else {
      st.add("stmts", "}");
    }
  }

  private void buildStmtException(final ST st, final StmtException stmt) {
    st.add("stmts", String.format("exception(\"%s\");", stmt.getMessage()));
  }

  private void buildStmtMark(final ST st, final StmtMark stmt) {
    st.add("stmts", String.format("mark(\"%s\");", stmt.getName()));
  }

  private void buildStmtTrace(final ST st, final StmtTrace stmt) {
    // TODO
  }
}
