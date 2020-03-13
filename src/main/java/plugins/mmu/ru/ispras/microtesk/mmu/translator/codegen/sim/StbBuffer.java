/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.castle.codegen.StringTemplateBuilder;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.*;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.*;
import ru.ispras.microtesk.mmu.model.spec.MmuBuffer.Kind;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

final class StbBuffer extends StbCommon implements StringTemplateBuilder {
  private static final String DATA_NAME = "data";

  private final Ir ir;
  private final Buffer buffer;
  private final Buffer next;

  private final BuildStrategy strategy;

  public StbBuffer(
      final String packageName,
      final Ir ir,
      final Buffer buffer,
      final boolean isTargetBuffer) {

    super(packageName);
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(buffer);

    this.ir = ir;
    this.buffer = buffer;
    this.next = getNext(buffer);

    switch (buffer.getKind()) {
      case MEMORY:
        InvariantChecks.checkFalse(isTargetBuffer);
        this.strategy = new MemoryStrategy();
        break;

      case REGISTER:
        InvariantChecks.checkFalse(isTargetBuffer);
        this.strategy = new RegisterStrategy();
        break;

      case UNMAPPED:
        this.strategy = isTargetBuffer ? new TargetStrategy() : new UnmappedStrategy();
        break;

      default:
        InvariantChecks.checkTrue(false);
        this.strategy = null;
        break;
    }
  }

  private static Buffer getNext(final Buffer buffer) {
    Buffer parent = buffer;

    while (null != parent.getNext()) {
      parent = parent.getNext();
    }

    return parent;
  }

  @Override
  protected String getId() {
    return buffer.getId();
  }

  @Override
  public ST build(final STGroup group) {
    ExprPrinter.get().addVariableMapping(buffer.getId(), buffer.getId() + ".get()");
    ExprPrinter.get().pushVariableScope();

    ExprPrinter.get().addVariableMappings(
        buffer.getAddressArg(), removePrefix(buffer.getAddressArg().getName()));
    ExprPrinter.get().addVariableMappings(
        buffer.getDataArg(), DATA_NAME);

    final ST st = group.getInstanceOf("source_file");
    st.add("instance", "instance");

    buildAccessor(st, group);
    strategy.build(st, group);

    ExprPrinter.get().popVariableScope();
    return st;
  }

  private void buildEntry(final ST st, final STGroup group) {
    if (buffer.isView()) {
      return;
    }

    final ST stEntry = group.getInstanceOf("buffer_entry");

    final Type type = buffer.getEntry();
    StbStruct.buildFields(stEntry, group, "Entry", type);

    st.add("members", stEntry);
  }

  private void buildAccessor(final ST st, final STGroup group) {
    final ST accessor = group.getInstanceOf("buffer_access_point");

    final String poolName = "instancePool";
    accessor.add("name", poolName);
    accessor.add("type", buffer.getId());
    accessor.add("init", String.format("%s = Collections.singletonList(new %s());", poolName, buffer.getId()));

    st.add("imps", "java.util.Collections");
    st.add("imps", "java.util.List");
    st.add("members", accessor);
  }

  private void buildIndexer(final ST st, final STGroup group) {
    buildNewLine(st);
    final ST stIndexer = group.getInstanceOf("buffer_indexer");

    stIndexer.add("addr_type", buffer.getAddress().getId());
    stIndexer.add("addr_name", removePrefix(buffer.getAddressArg().getName()));
    stIndexer.add("expr", indexToString(buffer.getIndex()));

    st.add("members", stIndexer);
  }

  private void buildMatcher(final ST st, final STGroup group) {
    buildNewLine(st);
    final ST stMatcher = group.getInstanceOf("buffer_matcher");

    stMatcher.add("entry_type", String.format("%s.Entry",
        buffer.isView() ? next.getId() : buffer.getId()));
    stMatcher.add("addr_type", buffer.getAddress().getId());
    stMatcher.add("addr_name", removePrefix(buffer.getAddressArg().getName()));
    stMatcher.add("data_name", DATA_NAME);
    stMatcher.add("expr", matchToExprString(buffer.getMatch()));
    stMatcher.add("stmts", matchToStmtStrings(buffer.getMatch(), buffer.getAddressArg().getName()));

    st.add("members", stMatcher);
  }

  private void buildConstructor(final ST st, final STGroup group) {
    buildNewLine(st);
    final ST stConstructor = group.getInstanceOf("buffer_constructor");

    stConstructor.add("name", buffer.getId());
    stConstructor.add("is_reg", buffer.getKind() == Kind.REGISTER);
    stConstructor.add("is_mem", buffer.getKind() == Kind.MEMORY);
    stConstructor.add("entry_type",
        String.format("%s.Entry", buffer.isView() ? next.getId() : buffer.getId()));
    stConstructor.add("addr_type", buffer.getAddress().getId());
    stConstructor.add("ways", buffer.getWays());
    stConstructor.add("sets", buffer.getSets());
    stConstructor.add("next", buffer.getNext() != null && !buffer.isView()
        ? String.format("%s.get()", buffer.getNext().getId())
        : "null");

    stConstructor.add("policy", String.format("%s.create(%s.%s, %s.%s, %s.%s, %s.%s)",
        POLICY_CLASS.getSimpleName(),
        EVICT_POLICY_ID_CLASS.getSimpleName(), buffer.getPolicy().eviction.name(),
        WRITE_POLICY_ID_CLASS.getSimpleName(), buffer.getPolicy().write.name(),
        INCLUSION_POLICY_ID_CLASS.getSimpleName(), buffer.getPolicy().inclusion.name(),
        COHERENCE_PROTOCOL_ID_CLASS.getSimpleName(), buffer.getPolicy().coherence.name()));

    st.add("members", stConstructor);
  }

  private void buildGetDataSize(ST st, STGroup group) {
    buildNewLine(st);
    final ST stMethod = group.getInstanceOf("get_data_size");
    stMethod.add("size", buffer.getDataArg().getBitSize());
    st.add("members", stMethod);
  }

  private String indexToString(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isConstant(expr)) {
      final NodeValue value = (NodeValue) expr;
      if (value.isType(DataTypeId.LOGIC_INTEGER)) {
        return ExprPrinter.bitVectorToString(BitVector.valueOf(
            value.getInteger(), buffer.getAddressArg().getBitSize()));
      }
      throw new IllegalArgumentException(
          String.format("Illegal index expression: %s", value));
    }

    return ExprPrinter.get().toString(expr);
  }

  private String matchToExprString(final Node expr) {
    InvariantChecks.checkNotNull(expr);

    if (ExprUtils.isConstant(expr)) {
      final NodeValue value = (NodeValue) expr;
      if (value.isType(DataTypeId.LOGIC_BOOLEAN)) {
        return Boolean.toString(value.getBoolean());
      } else if (value.isType(DataTypeId.LOGIC_INTEGER)
          && value.getInteger().equals(BigInteger.ZERO)) {
        return Boolean.toString(true);
      } else {
        throw new IllegalArgumentException(
            String.format("Illegal match expression: %s", value));
      }
    }

    return ExprPrinter.get().toString(expr);
  }

  private final class AssignTagExtractor extends ExprTreeVisitorDefault {
    private final String addressVar;
    private final Collection<String> assigns = new ArrayList<>();

    public AssignTagExtractor(final String addressVar) {
      this.addressVar = addressVar;
    }

    public Collection<String> getAssigns() {
      return assigns;
    }

    @Override
    public void onBegin() {
      assigns.clear();
    }

    @Override
    public void onOperationBegin(final NodeOperation node) {
      if (node.getOperationId() == StandardOperation.EQ) {
        final Node operand0 = node.getOperand(0);
        final Node operand1 = node.getOperand(1);

        final boolean isTag0 = !isConstantOrDependsOnAddress(operand0);
        final boolean isTag1 = !isConstantOrDependsOnAddress(operand1);

        if (isTag0 != isTag1) {
          final Node lhs = isTag0 ? operand0 : operand1;
          final Node rhs = isTag0 ? operand1 : operand0;

          assigns.add(String.format("%s.assign(%s);",
                  ExprPrinter.get().toString(lhs),
                  ExprPrinter.get().toString(rhs)));
        }

        setStatus(Status.SKIP);
      }
    }

    private final boolean isConstantOrDependsOnAddress(final Node node) {
      final Collection<NodeVariable> variables = ExprUtils.getVariables(node);
      for (final NodeVariable variable : variables) {
        final String name = variable.getName();

        if (!name.equals(addressVar) && !name.startsWith(addressVar + ".")) {
          return false;
        }
      }

      return true;
    }
  }

  private Collection<String> matchToStmtStrings(final Node expr, final String addr) {
    final AssignTagExtractor visitor = new AssignTagExtractor(addr);
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    // Extracts the tag assignments from the match predicate.
    walker.visit(expr);

    return visitor.getAssigns();
  }

  private interface BuildStrategy {
    void build(ST st, STGroup group);
  }

  private final class TargetStrategy implements BuildStrategy {
    @Override
    public void build(final ST st, final STGroup group) {
      buildHeader(st);
      buildEntry(st, group);
      buildMemoryConstructor(st, group);
      buildGetDataSize(st, group);
    }

    private void buildHeader(final ST st) {
      final String baseName = String.format("%s<%s, %s>",
          MEMORY_CLASS.getSimpleName(),
          String.format("%s.Entry", next.getId()),
          buffer.getAddress().getId()
          );

      StbBuffer.this.buildHeader(st, baseName);
    }

    private void buildMemoryConstructor(final ST st, final STGroup group) {
      final ST stConstructor = group.getInstanceOf("memory_constructor");

      final BigInteger ways = buffer.getWays();
      final BigInteger sets = buffer.getSets();
      final BigInteger entries = ways.multiply(sets);

      InvariantChecks.checkTrue(buffer.getEntry().getBitSize() % 8 == 0);
      final int entryByteSize = buffer.getEntry().getBitSize() / 8;

      final BigInteger byteSize = entries.multiply(BigInteger.valueOf(entryByteSize));
      stConstructor.add("entry_type",
          String.format("%s.Entry", buffer.isView() ? next.getId() : buffer.getId()));
      stConstructor.add("addr_type", buffer.getAddress().getId());
      stConstructor.add("size", byteSize.toString(16));

      st.add("members", stConstructor);
    }
  }

  private final class UnmappedStrategy implements BuildStrategy {
    @Override
    public void build(final ST st, final STGroup group) {
      buildHeader(st);
      buildEntry(st, group);
      buildIndexer(st, group);
      buildMatcher(st, group);
      buildConstructor(st, group);
    }

    private void buildHeader(final ST st) {
      final String baseName = String.format("%s<%s, %s>",
          CACHE_CLASS.getSimpleName(),
          String.format("%s.Entry", buffer.isView() ? next.getId() : buffer.getId()),
          buffer.getAddress().getId()
          );

      StbBuffer.this.buildHeader(st, baseName);
    }
  }

  private final class MemoryStrategy implements BuildStrategy {
    @Override
    public void build(final ST st, final STGroup group) {
      buildHeader(st);
      buildEntry(st, group);
      buildIndexer(st, group);
      buildMatcher(st, group);
      buildConstructor(st, group);
      buildGetMmu(st, group);
      buildGetDataSize(st, group);
    }

    private void buildHeader(final ST st) {
      final String baseName = String.format("%s<%s, %s>",
          MMU_MAPPING_CLASS.getSimpleName(),
          String.format("%s.Entry", buffer.isView() ? next.getId() : buffer.getId()),
          buffer.getAddress().getId()
          );

      StbBuffer.this.buildHeader(st, baseName);
    }

    private void buildGetMmu(final ST st, final STGroup group) {
      buildNewLine(st);
      final ST stMethod = group.getInstanceOf("get_mmu");

      InvariantChecks.checkTrue(ir.getMemories().size() == 1);
      final Memory memory = ir.getMemories().values().iterator().next();

      stMethod.add("addr_type", buffer.getAddress().getId());
      stMethod.add("mmu_name", memory.getId());

      st.add("members", stMethod);
    }
  }

  private final class RegisterStrategy implements BuildStrategy {
    @Override
    public void build(final ST st, final STGroup group) {
      buildHeader(st);
      buildEntry(st, group);
      buildIndexer(st, group);
      buildMatcher(st, group);
      buildConstructor(st, group);
      buildGetDataSize(st, group);
    }

    private void buildHeader(final ST st) {
      final String baseName = String.format("%s<%s, %s>",
          REG_MAPPING_CLASS.getSimpleName(),
          String.format("%s.Entry", buffer.isView() ? next.getId() : buffer.getId()),
          buffer.getAddress().getId()
          );

      StbBuffer.this.buildHeader(st, baseName);
    }
  }
}
