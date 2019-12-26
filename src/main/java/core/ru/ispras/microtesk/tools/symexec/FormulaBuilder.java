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
import ru.ispras.microtesk.translator.mir.MirArchive;
import ru.ispras.microtesk.translator.mir.MirBuilder;
import ru.ispras.microtesk.translator.mir.MirContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.json.JsonObject;

public final class FormulaBuilder {
  public static MirContext buildMir(
      final String name,
      final Model m,
      final MirArchive archive,
      final List<IsaPrimitive> insns) {
    final MirBuilder builder = new MirBuilder();
    for (final IsaPrimitive insn : insns) {
      final IsaInstance p = new IsaInstance(m, insn);
      buildOperand(builder, p);

      final String callee = String.format("%s.action", insn.getName());
      builder.makeCall(callee, 0);
    }
    final JsonObject pcInfo = archive.getManifest().getJsonObject("program_counter");
    builder.refMemory(pcInfo.getInt("size"), pcInfo.getString("name"));

    return builder.build(name);
  }

  public static MirContext buildMir(
      final String name, final MirArchive archive, final List<MirContext> body) {
    final MirBuilder builder = new MirBuilder();
    for (final MirContext mir : body) {
      builder.makeThisCall(mir.name, 0);
    }
    final JsonObject pcInfo = archive.getManifest().getJsonObject("program_counter");
    builder.refMemory(pcInfo.getInt("size"), pcInfo.getString("name"));

    return builder.build(name);
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
