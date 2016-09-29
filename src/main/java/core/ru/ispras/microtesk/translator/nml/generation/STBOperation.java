/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import static ru.ispras.microtesk.translator.generation.PackageInfo.MODE_CLASS_FORMAT;
import static ru.ispras.microtesk.translator.generation.PackageInfo.OP_PACKAGE_FORMAT;
import static ru.ispras.microtesk.translator.generation.PackageInfo.SHARED_CLASS_FORMAT;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.Immediate;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut.Argument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;

final class STBOperation extends STBPrimitiveBase {
  private final String modelName;
  private final PrimitiveAND op;

  private boolean modesImported = false;
  private boolean immsImported = false;

  private void importModeDependencies(final ST t) {
    if (!modesImported) {
      t.add("imps", String.format(MODE_CLASS_FORMAT, modelName, "*"));
      modesImported = true;
    }
  }

  private void importImmDependencies(final ST t) {
    if (!immsImported) {
      t.add("imps", Location.class.getName());
      // t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
      immsImported = true;
    }
  }

  public STBOperation(final String modelName, final PrimitiveAND op) {
    assert op.getKind() == Primitive.Kind.OP;

    this.modelName = modelName;
    this.op = op;
  }

  private void buildHeader(final ST t) {
    t.add("name", op.getName());
    t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));

    t.add("imps", Map.class.getName());
    t.add("imps", BigInteger.class.getName());
    t.add("imps", ArgumentMode.class.getName());
    t.add("imps", ru.ispras.microtesk.model.api.Execution.class.getName());
    t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", Location.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", Operation.class.getPackage().getName()));
    t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

    t.add("base", Operation.class.getSimpleName());
  }

  private void buildArguments(final STGroup group, final ST t) {
    for (final Map.Entry<String, Primitive> e : op.getArguments().entrySet()) {
      final String argName = e.getKey();
      final Primitive argType = e.getValue();

      t.add("arg_names", argName);

      switch (argType.getKind()) {
        case IMM:
          t.add("arg_tnames", argType.getName());
          break;

        case MODE:
        case OP:
          t.add("arg_tnames", String.format("%s.INFO", argType.getName()));
          break;
      }

      /*t.add(
          "arg_tnames",
          Primitive.Kind.IMM == argType.getKind() ? argType.getName() : String.format("%s.INFO",
              argType.getName()));*/

      final ST argCheckST;
      if (Primitive.Kind.MODE == argType.getKind()) {
        importModeDependencies(t);
        t.add("arg_types",
            argType.isOrRule() ? AddressingMode.class.getSimpleName() : argType.getName());

        argCheckST = group.getInstanceOf("op_arg_check_opmode");
      } else if (Primitive.Kind.OP == argType.getKind()) {
        t.add( "arg_types",
            argType.isOrRule() ? Operation.class.getSimpleName() : argType.getName());

        argCheckST = group.getInstanceOf("op_arg_check_opmode");
      } else // if Primitive.Kind.IMM == oa.getKind()
      {
        importImmDependencies(t);
        t.add("arg_types", Immediate.class.getSimpleName());

        argCheckST = group.getInstanceOf("op_arg_check_imm");
      }

      argCheckST.add("arg_name", argName);
      argCheckST.add("arg_type", argType.getName());

      t.add("arg_checks", argCheckST);
    }
  }

  private void buildAttributes(final STGroup group, final ST t) {
    for (final Attribute attr : op.getAttributes().values()) {
      final ST attrST = group.getInstanceOf("op_attribute");

      attrST.add("name", attr.getName());
      attrST.add("rettype", getRetTypeName(attr.getKind()));

      if (Attribute.Kind.ACTION == attr.getKind()) {
        for (Statement stmt : attr.getStatements()) {
          addStatement(attrST, stmt, false);
        }
      } else if (Attribute.Kind.EXPRESSION == attr.getKind()) {
        final int count = attr.getStatements().size();
        int index = 0;
        for (final Statement stmt : attr.getStatements()) {
          addStatement(attrST, stmt, index == count - 1);
          index++;
        }
      } else {
        assert false : "Unknown attribute kind: " + attr.getKind();
      }

      attrST.add("override", attr.isStandard());
      t.add("attrs", attrST);
    }
  }

  private void buildShortcuts(final STGroup group, final ST t) {
    for (final Shortcut shortcut : op.getShortcuts()) {
      //ContextBuilder.process(shortcut);
 
      final ST shortcutST = group.getInstanceOf("shortcut");

      shortcutST.add("name", op.getName());
      shortcutST.add("entry", shortcut.getEntry().getName());

      for (final Shortcut.Argument arg : shortcut.getArguments()) {
        final Primitive argType = arg.getType();

        shortcutST.add("arg_names", arg.getUniqueName());

        switch (argType.getKind()) {
          case IMM:
            shortcutST.add("arg_tnames", argType.getName());
            break;

          case MODE:
          case OP:
            shortcutST.add("arg_tnames", String.format("%s.INFO", argType.getName()));
            break;
        }

        /*
        shortcutST.add("arg_tnames", Primitive.Kind.IMM == argType.getKind() ? argType.getName()
            : String.format("%s.INFO", argType.getName()));
        */

        if (Primitive.Kind.MODE == argType.getKind()) {
          importModeDependencies(t);
          shortcutST.add("arg_types",
              argType.isOrRule() ? AddressingMode.class.getSimpleName() : argType.getName());
        } else if (Primitive.Kind.OP == argType.getKind()) {
          shortcutST.add("arg_types",
              argType.isOrRule() ? Operation.class.getSimpleName() : argType.getName());
        } else // if Primitive.Kind.IMM == oa.getKind()
        {
          importImmDependencies(t);
          shortcutST.add("arg_types", Immediate.class.getSimpleName());
        }
      }

      shortcutST.add("op_tree",
          createOperationTreeST(group, shortcut.getEntry(), shortcut.getArguments()));

      t.add("shortcuts", shortcutST);

      final ST shortcutDefST = group.getInstanceOf("shortcut_def");
      shortcutDefST.add("entry", shortcut.getEntry().getName());

      for (final String context : shortcut.getContextName())
        shortcutDefST.add("contexts", context);

      t.add("shortcut_defs", shortcutDefST);
    }
  }
  
  static final class ContextBuilder {
    private final Shortcut shortcut;

    public static void process(final Shortcut shortcut) {
      new ContextBuilder(shortcut).dump();
    }

    private ContextBuilder(final Shortcut shortcut) {
      this.shortcut = shortcut;
    }

    public void dump() {
      System.out.println(shortcut);
      dump(shortcut.getEntry().getName(), shortcut.getEntry());
    }

    private void dump(final String prefix, final PrimitiveAND primitive) {
      for (final Map.Entry<String, Primitive> e : primitive.getArguments().entrySet()) {
        final String argName = e.getKey();
        final Primitive argValue = e.getValue();

        final String variableName = String.format("%s.%s", prefix, argName);
        final Shortcut.Argument sa = findShortcutArgument(argName, primitive);
        if (null != sa) {
          System.out.printf("prefix for %s -> %s%n", sa.getUniqueName(), prefix);
        } else {
          System.out.printf("link to %s -> %s%n", variableName, argValue.getName());

        if (argValue instanceof PrimitiveAND) {
          dump(variableName, (PrimitiveAND) argValue);
        }
        }
      }
    }

    private Shortcut.Argument findShortcutArgument(final String name, final PrimitiveAND source) {
      for(final Shortcut.Argument sa : shortcut.getArguments()) {
        if (name.equals(sa.getName()) && source.getName().equals(sa.getSource().getName())) {
          return sa;
        }
      }

      return null;
    }
  }

  private ST createOperationTreeST(
      final STGroup group,
      final PrimitiveAND root,
      final Collection<Argument> args) {
    final ST t = group.getInstanceOf("op_tree_node");
    t.add("name", root.getName());

    for (final Map.Entry<String, Primitive> e : root.getArguments().entrySet()) {
      if (e.getValue().getKind() == Primitive.Kind.MODE) {
        t.add("params", getUniqueArgumentName(e, args));
      } else if (e.getValue().getKind() == Primitive.Kind.OP) {
        assert !e.getValue().isOrRule() : String.format("%s is an OR rule: %s", e.getKey(), e
            .getValue().getName());
        t.add("params", createOperationTreeST(group, (PrimitiveAND) e.getValue(), args));
      } else {
        t.add("params", getUniqueArgumentName(e, args));
      }
    }

    return t;
  }

  private String getUniqueArgumentName(
      final Map.Entry<String, Primitive> arg,
      final Collection<Argument> arg_defs) {
    for (final Argument a : arg_defs) {
      if (a.getName().equals(arg.getKey())
          || a.getSource().getName().equals(arg.getValue().getName()))
        return a.getUniqueName();
    }

    assert false : "Failed to find a unique name.";
    return arg.getKey();
  }

  @Override
  public ST build(final STGroup group) {
    final ST t = group.getInstanceOf("op");

    buildHeader(t);
    buildArguments(group, t);
    buildAttributes(group, t);
    buildShortcuts(group, t);

    return t;
  }
}
