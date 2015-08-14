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
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Stmt;
import ru.ispras.microtesk.mmu.translator.ir.StmtAssign;
import ru.ispras.microtesk.mmu.translator.ir.StmtException;
import ru.ispras.microtesk.mmu.translator.ir.StmtIf;
import ru.ispras.microtesk.mmu.translator.ir.StmtMark;
import ru.ispras.microtesk.mmu.translator.ir.StmtTrace;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.utils.FormatMarker;

public abstract class STBCommon {
  public static final Class<?> BIT_VECTOR_CLASS =
      ru.ispras.fortress.data.types.bitvector.BitVector.class;

  public static final Class<?> ADDRESS_CLASS =
      ru.ispras.microtesk.mmu.model.api.Address.class;

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

  protected STBCommon(final String packageName) {
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
    st.add("ext", base);
    st.add("pack", packageName);

    st.add("imps", String.format("%s.*", BUFFER_CLASS.getPackage().getName()));
    st.add("imps", String.format("%s.*", BIT_VECTOR_CLASS.getPackage().getName()));
  }

  protected final void buildVariableDecls(final ST st, final Collection<Variable> variables) {
    for (final Variable variable : variables) {
      final String name = removePrefix(variable.getName());
      final Type type = variable.getType();

      final String typeName;
      final String value;

      if (type.getId() != null) {
        typeName = type.getId();
        value = String.format("new %s()", typeName);
      } else {
        typeName = BIT_VECTOR_CLASS.getSimpleName();
        value = type.getDefaultValue() != null ?
            ExprPrinter.bitVectorToString(type.getDefaultValue()) :
            String.format("%s.newEmpty(%d)", typeName, type.getBitSize());
      }

      ExprPrinter.get().addVariableMappings(variable, name);
      st.add("stmts", String.format("final %s %s = %s;", typeName,  name, value));
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
    final Node left = stmt.getLeft();
    final Node right = correctRightType(stmt.getRight(), left);

    final String leftText = ExprPrinter.get().toString(left);
    final String rightText = ExprPrinter.get().toString(right);
    final String rightSuffix = getRightFieldSuffix(left, right);

    final String stmtText =
        String.format("%s.assign(%s%s);", leftText, rightText, rightSuffix);

    st.add("stmts", stmtText);
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

  // Handles a situation when a bit vector is assigned a structure.
  private String getRightFieldSuffix(final Node left, final Node right) {
    final Type leftType = getType(left);
    final Type rightType = getType(right);

    // When on of the types is undefined, we can make no decision on type cast.
    if (null == leftType || null == rightType) {
      return "";
    }

    // If left is a bit vector and right is a struct, right is converted to a bit vector.
    if (!leftType.isStruct() && rightType.isStruct()) {
      return ".asBitVector()";
    }

    return "";
  }

  private static Type getType(final Node expr) {
    final Object userData = expr.getUserData();
    if (userData instanceof Variable) {
      return ((Variable) userData).getType();
    } else if (userData instanceof AttributeRef) {
      final AttributeRef attrRef = (AttributeRef) userData;
      return attrRef.getTarget().getDataArg().getType();
    } else if (userData == null) {
      return null;
    } else {
      throw new IllegalArgumentException(String.format(
          "Unknown userData type of %s: %s", expr, userData));
    }
  }

  private void buildStmtIf(final ST st, final STGroup group, final StmtIf stmt) {
    boolean isFirst = true;
    for (final Pair<Node, List<Stmt>> block : stmt.getIfBlocks()) {
      final Node condition = block.first;
      final List<Stmt> stmts = block.second;

      if (condition.equals(NodeValue.newBoolean(true))) {
        // Add statements to the current level
        buildStmts(st, group, stmts);
      } else if (condition.equals(NodeValue.newBoolean(false))) {
        // Ignore the block and its statements 
      } else {
        // Create a new block
        final ST stIf = group.getInstanceOf(isFirst ? "if_block" : "elseif_block");
        isFirst = false;

        final String exprText = ExprPrinter.get().toString(condition);
        stIf.add("expr", exprText);

        buildStmts(stIf, group, stmts);
        st.add("stmts", stIf);
      }
    }

    if (isFirst) {
      // If no "IF" block was created, add statements of "ELSE" to the current level
      buildStmts(st, group, stmt.getElseBlock());
    } else {
      if (!stmt.getElseBlock().isEmpty()) {
        final ST stElse = group.getInstanceOf("else_block");
        buildStmts(stElse, group, stmt.getElseBlock());
        st.add("stmts", stElse);
      } else {
        st.add("stmts", "}");
      }
    }
  }

  private void buildStmtException(final ST st, final StmtException stmt) {
    st.add("stmts", String.format("exception(\"%s\");", stmt.getMessage()));
  }

  private void buildStmtMark(final ST st, final StmtMark stmt) {
    st.add("stmts", String.format("mark(\"%s\");", stmt.getName()));
  }

  private void buildStmtTrace(final ST st, final StmtTrace stmt) {
    final StringBuilder sb = new StringBuilder();
    sb.append(String.format("trace(\"%s\"", stmt.getFormat()));

    for (int index = 0; index < stmt.getArguments().size(); ++index) {
      final FormatMarker marker = stmt.getMarkers().get(index);

      final Node argument = stmt.getArguments().get(index);
      final String argumentText = ExprPrinter.get().toString(argument);

      final boolean isBitVector;
      if (argument instanceof NodeValue) {
        isBitVector = argument.isType(DataTypeId.BIT_VECTOR);
      } else if (argument instanceof NodeOperation) {
        isBitVector = argument.isType(DataTypeId.BIT_VECTOR) || 
                      argument.isType(DataTypeId.UNKNOWN);
      } else if (argument instanceof NodeVariable) {
        final Type type = getType(argument);
        isBitVector = !type.isStruct();
      } else {
        throw new IllegalStateException("Unexpected node type: " + argument);
      }

      final String suffix;
      if (marker == FormatMarker.BIN) {
        suffix = isBitVector ? ".toBinString()" : "";
      } else if (marker == FormatMarker.DEC) { 
        suffix = isBitVector ? ".bigIntegerValue()" : "";
      } else if (marker == FormatMarker.HEX) {
        suffix = isBitVector ? ".toHexString()" : "";
      } else if (marker == FormatMarker.STR) {
        suffix = isBitVector ? ".toString()" : "";
      } else {
        throw new IllegalStateException("Unknown format marker: " + marker);
      }

      sb.append(", ");
      sb.append(argumentText);
      sb.append(suffix);
    }

    sb.append(");");
    st.add("stmts", sb.toString());
  }
}
