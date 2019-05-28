/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.simc;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.translator.codegen.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.nml.ir.primitive.Shortcut.Argument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

public final class StbOperation extends StbPrimitiveBase {
  private final String modelName;
  private final PrimitiveAnd op;

  private boolean modesImported = false;
  private boolean immsImported = false;

  private void importModeDependencies(final ST t) {
    if (!modesImported) {
      t.add("imps", String.format(PackageInfo.MODE_CLASS_FORMAT, modelName, "*"));
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

  public StbOperation(final String modelName, final PrimitiveAnd op) {
    assert op.getKind() == Primitive.Kind.OP;

    this.modelName = modelName;
    this.op = op;
  }

  private void buildHeader(final ST t) {
    t.add("name", op.getName());
    //t.add("pack", String.format(PackageInfo.OP_PACKAGE_FORMAT, modelName));

    t.add("imps", Map.class.getName());
    t.add("imps", BigInteger.class.getName());
    t.add("imps", ru.ispras.microtesk.model.Execution.class.getName());
    t.add("imps", ru.ispras.microtesk.model.ProcessingElement.class.getName());
    t.add("imps", String.format("%s.*", Data.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", Location.class.getPackage().getName()));
    t.add("imps", String.format("%s.*", IsaPrimitive.class.getPackage().getName()));
    t.add("imps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".PE", modelName));
    t.add("imps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TempVars", modelName));
    //t.add("simps", String.format(PackageInfo.MODEL_PACKAGE_FORMAT + ".TypeDefs", modelName));

    //t.add("base", IsaPrimitive.class.getSimpleName());

    if (op.getModifier() == Primitive.Modifier.PSEUDO) {
      importModeDependencies(t);
    }
  }

  private void buildArguments(final STGroup group, final ST t) {
    final int arg_count = op.getArguments().size();
    t.add("arg_count", String.valueOf(arg_count));
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

      if (Primitive.Kind.MODE == argType.getKind()) {
        importModeDependencies(t);
        t.add("arg_types",
            argType.isOrRule() ? IsaPrimitive.class.getSimpleName() : argType.getName());
      } else if (Primitive.Kind.OP == argType.getKind()) {
        t.add( "arg_types",
            argType.isOrRule() ? IsaPrimitive.class.getSimpleName() : argType.getName());
      } else { // if Primitive.Kind.IMM == oa.getKind()
        importImmDependencies(t);
        t.add("arg_types", Immediate.class.getSimpleName());
      }
    }
  }

  private void buildAttributes(final STGroup group, final ST t) {
    for (final Attribute attr : op.getAttributes().values()) {
      final ST attrST = group.getInstanceOf("op_attribute");

      attrST.add("name", attr.getName());
      attrST.add("rettype", getRetTypeName(attr.getKind()));
      attrST.add("usePE",
          Attribute.Kind.ACTION == attr.getKind()
              && !attr.getName().equals(Attribute.INIT_NAME)
              && !attr.getName().equals(Attribute.DECODE_NAME));

      if (Attribute.Kind.ACTION == attr.getKind()) {
        for (final Statement stmt : attr.getStatements()) {
          if (isModeInstanceUsed(stmt)) {
            importModeDependencies(t);
          }

          addStatement(attrST, stmt, false);
        }
      } else if (Attribute.Kind.EXPRESSION == attr.getKind()) {
        final int count = attr.getStatements().size();
        int index = 0;
        for (final Statement stmt : attr.getStatements()) {
          if (isModeInstanceUsed(stmt)) {
            importModeDependencies(t);
          }

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

  private static boolean isModeInstanceUsed(final Statement stmt) {
    if (!(stmt instanceof StatementAttributeCall)) {
      return false;
    }

    final StatementAttributeCall call = (StatementAttributeCall) stmt;
    if (null == call.getCalleeInstance()) {
      return false;
    }

    final Instance instance = call.getCalleeInstance();
    return isModeInstanceUsed(instance);
  }

  private static boolean isModeInstanceUsed(final Instance instance) {
    if (instance.getPrimitive().getKind() == Primitive.Kind.MODE) {
      return true;
    }

    for (final InstanceArgument argument : instance.getArguments()) {
      switch (argument.getKind()) {
        case INSTANCE: {
          if (isModeInstanceUsed(argument.getInstance())) {
            return true;
          }
        }

        case PRIMITIVE: {
          if (argument.getPrimitive().getKind() == Primitive.Kind.MODE) {
            return true;
          }
        }

        default: {
          // Nothing
        }
      }
    }

    return false;
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
              argType.isOrRule() ? IsaPrimitive.class.getSimpleName() : argType.getName());
        } else if (Primitive.Kind.OP == argType.getKind()) {
          shortcutST.add("arg_types",
              argType.isOrRule() ? IsaPrimitive.class.getSimpleName() : argType.getName());
        } else { // if Primitive.Kind.IMM == oa.getKind()
          importImmDependencies(t);
          shortcutST.add("arg_types", Immediate.class.getSimpleName());
        }
      }

      //shortcutST.add("op_tree",
      //    createOperationTreeST(group, shortcut.getEntry(), shortcut.getArguments()));

      /*t.add("shortcuts", shortcutST);

      final ST shortcutDefST = group.getInstanceOf("shortcut_def");
      shortcutDefST.add("entry", shortcut.getEntry().getName());

      for (final String context : shortcut.getContextName()) {
        shortcutDefST.add("contexts", context);
      }

      t.add("shortcut_defs", shortcutDefST);*/
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

    private void dump(final String prefix, final PrimitiveAnd primitive) {
      for (final Map.Entry<String, Primitive> e : primitive.getArguments().entrySet()) {
        final String argName = e.getKey();
        final Primitive argValue = e.getValue();

        final String variableName = String.format("%s.%s", prefix, argName);
        final Shortcut.Argument sa = findShortcutArgument(argName, primitive);
        if (null != sa) {
          System.out.printf("prefix for %s -> %s%n", sa.getUniqueName(), prefix);
        } else {
          System.out.printf("link to %s -> %s%n", variableName, argValue.getName());

          if (argValue instanceof PrimitiveAnd) {
            dump(variableName, (PrimitiveAnd) argValue);
          }
        }
      }
    }

    private Shortcut.Argument findShortcutArgument(final String name, final PrimitiveAnd source) {
      for (final Shortcut.Argument sa : shortcut.getArguments()) {
        if (name.equals(sa.getName()) && source.getName().equals(sa.getSource().getName())) {
          return sa;
        }
      }

      return null;
    }
  }

  private ST createOperationTreeST(
      final STGroup group,
      final PrimitiveAnd root,
      final Collection<Argument> args) {
    final ST t = group.getInstanceOf("op_tree_node");
    t.add("name", root.getName());

    for (final Map.Entry<String, Primitive> e : root.getArguments().entrySet()) {
      if (e.getValue().getKind() == Primitive.Kind.MODE) {
        t.add("params", getUniqueArgumentName(e, args));
      } else if (e.getValue().getKind() == Primitive.Kind.OP) {
        assert !e.getValue().isOrRule() : String.format("%s is an OR rule: %s", e.getKey(), e
            .getValue().getName());
        t.add("params", createOperationTreeST(group, (PrimitiveAnd) e.getValue(), args));
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
          || a.getSource().getName().equals(arg.getValue().getName())) {
        return a.getUniqueName();
      }
    }

    assert false : "Failed to find a unique name.";
    return arg.getKey();
  }

  @Override
  public ST build(final STGroup group) {
    final ST t = group.getInstanceOf("op");
    t.add("isa_type", "OP");
    buildHeader(t);
    buildArguments(group, t);
    //buildAttributes(group, t);
    //buildShortcuts(group, t);

    return t;
  }
}
