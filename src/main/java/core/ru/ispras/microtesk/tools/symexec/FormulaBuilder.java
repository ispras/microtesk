/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.symexec;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.IsaPrimitiveKind;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaData;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.translator.mir.MirBuilder;
import ru.ispras.microtesk.translator.mir.MirContext;
import ru.ispras.microtesk.translator.nml.coverage.PathConstraintBuilder;
import ru.ispras.microtesk.translator.nml.coverage.SsaAssembler;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;
import ru.ispras.microtesk.utils.NamePath;

import ru.ispras.testbase.TestBaseContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FormulaBuilder {
  public static MirContext buildMir(final Model m, final List<IsaPrimitive> insns) {
    final MirBuilder builder = new MirBuilder();
    for (final IsaPrimitive insn : insns) {
      final IsaInstance p = new IsaInstance(m, insn);
      buildOperand(builder, p);

      final String callee = String.format("%s.action", insn.getName());
      builder.makeCall(callee, 0);
    }
    return builder.build("");
  }

  private static void buildOperand(final MirBuilder builder, final IsaInstance p) {
    if (p.kind.equals(IsaPrimitiveKind.IMM)) {
      final Location l = ((Immediate) p.item).access();
      builder.addValue(l.getBitSize(), l.getValue());
    } else {
      final Collection<IsaPrimitive> args = p.item.getArguments().values();
      for (final IsaPrimitive arg : args) {
        buildOperand(builder, p.adopt(arg));
      }
      builder.makeClosure(p.item.getName(), args.size());
    }
  }

  public static List<Node> buildFormulas(
      final Model model,
      final List<IsaPrimitive> sequence) {
    final SsaAssembler assembler = new SsaAssembler(TestBase.get().getStorage(model.getName()));
    final List<Node> formulae = new ArrayList<>(sequence.size());

    int n = 0;
    for (final IsaPrimitive p : sequence) {
      final String prefix = String.format("op_%d", n++, p.getName());
      final String tag = String.format("%s_%s", prefix, p.getName());

      final Map<String, Object> context = new HashMap<>();
      final Map<String, BitVector> literals = new LinkedHashMap<>();
      buildContext(model, context, literals, p);

      for (final Map.Entry<String, BitVector> e : literals.entrySet()) {
        final String name = String.format("%s_%s", prefix, e.getKey());
        final BitVector value = e.getValue();

        final Node variable = NodeVariable.newBitVector(name, value.getBitSize());
        variable.setUserData(1);

        formulae.add(Nodes.eq(variable, NodeValue.newBitVector(value)));
      }

      final Node f = assembler.assemble(context, p.getName(), tag);
      formulae.add(f);
    }

    final Constraint c = new PathConstraintBuilder(formulae).build();
    return ((Formulas) c.getInnerRep()).exprs();
  }

  private static void buildContext(
      final Model model,
      final Map<String, Object> context,
      final Map<String, BitVector> literals,
      final IsaPrimitive p) {
    buildContext(context, literals, NamePath.get(p.getName()), new IsaInstance(model, p));
    context.put(TestBaseContext.INSTRUCTION, p.getName());
  }

  private static void buildContext(
      final Map<String, Object> context,
      final Map<String, BitVector> literals,
      final NamePath prefix,
      final IsaInstance input) {
    for (final Map.Entry<String, IsaPrimitive> entry : input.item.getArguments().entrySet()) {
      final NamePath path;
      final NamePath guessedPath = prefix.resolve(entry.getKey());

      final IsaInstance arg = input.adopt(entry.getValue());
      if (!input.acceptsArgument(entry.getKey(), arg)) {
        final Pair<String, String> inter =
          input.substituteArgument(entry.getKey(), arg);
        context.put(guessedPath.toString(), inter.first);

        path = guessedPath.resolve(inter.second);
      } else {
        path = guessedPath;
      }

      final String key = path.toString();
      context.put(key, arg.item.getName());

      if (arg.kind.equals(IsaPrimitiveKind.IMM)) {
        final Location location = ((Immediate) arg.item).access();
        literals.put(key, BitVector.valueOf(location.getValue(), location.getBitSize()));
        // override context for immediates
        context.put(key, Immediate.TYPE_NAME);
      } else {
        buildContext(context, literals, path, arg);
      }
    }
  }

  private static final class IsaInstance {
    public final Model model;
    public final IsaPrimitive item;
    public final IsaPrimitiveKind kind;
    public final MetaData data;

    IsaInstance(final Model model, final IsaPrimitive item) {
      this.model = model;
      this.item = item;

      final MetaModel md = model.getMetaData();
      final MetaOperation op = md.getOperation(item.getName());
      final MetaAddressingMode mode = md.getAddressingMode(item.getName());

      if (op != null) {
        this.kind = IsaPrimitiveKind.OP;
        this.data = op;
      } else if (mode != null) {
        this.kind = IsaPrimitiveKind.MODE;
        this.data = mode;
      } else {
        this.kind = IsaPrimitiveKind.IMM;
        this.data = null;
      }
    }

    IsaInstance adopt(final IsaPrimitive item) {
      return new IsaInstance(this.model, item);
    }

    boolean acceptsArgument(final String name, final IsaInstance arg) {
      final MetaArgument param = getParameter(name);
      return param != null && param.isTypeAccepted(arg.getName());
    }

    String getName() {
      if (kind == IsaPrimitiveKind.IMM) {
        return Immediate.TYPE_NAME;
      }
      return item.getName();
    }

    Pair<String, String> substituteArgument(final String name, final IsaInstance arg) {
      final MetaArgument param = getParameter(name);
      if (param != null ) {
        switch (param.getKind()) {
        default: break;

        case OP:
          for (final String typeName : param.getTypeNames()) {
            final MetaOperation md = model.getMetaData().getOperation(typeName);
            final MetaArgument expected =
              findSingleton(md.getArguments(), arg.getName());
            if (expected != null) {
              return new Pair<>(typeName, expected.getName());
            }
          }
          break;

        case MODE:
          for (final String typeName : param.getTypeNames()) {
            final MetaAddressingMode md = model.getMetaData().getAddressingMode(typeName);
            final MetaArgument expected =
              findSingleton(md.getArguments(), arg.getName());
            if (expected != null) {
              return new Pair<>(typeName, expected.getName());
            }
          }
          break;
        }
      }
      throw new IllegalStateException("Appropriate argument substitution not found");
    }

    MetaArgument getParameter(final String name) {
      final MetaArgument param;
      switch (this.kind) {
      default:
        param = null;
        break;

      case OP:
        param = ((MetaOperation) this.data).getArgument(name);
        break;

      case MODE:
        param = ((MetaAddressingMode) this.data).getArgument(name);
        break;
      }
      return param;
    }

    private static MetaArgument findSingleton(final Iterable<MetaArgument> params, final String typeName) {
      final Iterator<MetaArgument> it = params.iterator();
      if (it.hasNext()) {
        final MetaArgument param = it.next();
        if (param.isTypeAccepted(typeName) && !it.hasNext()) {
          return param;
        }
      }
      return null;
    }
  }
}
