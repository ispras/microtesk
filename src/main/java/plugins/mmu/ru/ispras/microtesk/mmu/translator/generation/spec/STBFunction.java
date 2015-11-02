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

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.translator.generation.STBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class STBFunction implements STBuilder {
  public static final Class<?> EXPRESSION_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression.class;

  public static final Class<?> EXTERN_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuExternVariable.class;

  public static final Class<?> INTEGER_CLASS =
      ru.ispras.microtesk.basis.solver.integer.IntegerVariable.class;

  public static final Class<?> SPEC_CLASS =
      ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem.class;

  private final String packageName;
  private final Ir ir;
  private final Callable func;

  protected STBFunction(final String packageName, final Ir ir, final Callable func) {
    InvariantChecks.checkNotNull(packageName);
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(func);

    this.packageName = packageName;
    this.ir = ir;
    this.func = func;
  }

  @Override
  public ST build(final STGroup group) {
    final ST st = group.getInstanceOf("source_file");

    buildHeader(st);
    // buildArguments(st, group);
    // buildConstructor(st, group);
    buildFunction(st, group);

    return st;
  }

  private void buildHeader(final ST st) {
    st.add("name", func.getName()); 
    st.add("pack", packageName);
    st.add("instance", "INSTANCE");

    st.add("imps", INTEGER_CLASS.getName());
    st.add("imps", EXPRESSION_CLASS.getName());
    st.add("imps", SPEC_CLASS.getName());

    if (!ir.getExterns().isEmpty()) {
      st.add("imps", EXTERN_CLASS.getName());
      st.add("imps", packageName.substring(0,  packageName.lastIndexOf('.')) + ".sim.Extern");
    }
  }

  private static List<String> getParameterList(final Callable func) {
    final List<String> decls = new ArrayList<>();
    if (func.getOutput() != null) {
      decls.add(getVarDecl(func.getName(), func.getOutput()));
    }
    for (final Variable var : func.getParameters()) {
      decls.add(getVarDecl(func.getName(), var));
    }
    return decls;
  }

  private static String getVarDecl(final String prefix, final Variable var) {
    return String.format(
        "%s %s",
        getTypeName(var.getType()),
        Utils.getVariableName(prefix, var.getName()));
  }

  private static String getTypeName(final Type type) {
    return (type.isStruct()) ? type.getId() : INTEGER_CLASS.getSimpleName();
  }

  private String getVariableName(final String prefixedName) {
    return Utils.getVariableName(func.getName(), prefixedName);
  }

  private void buildFunction(final ST st, final STGroup group) {
    ControlFlowBuilder.buildImports(st, group);
    st.add("imps", java.util.ArrayList.class.getName());
    st.add("imps", java.util.List.class.getName());

    final ST stFunction = group.getInstanceOf("function");
    stFunction.add("name", func.getName());

    for (final String decl : getParameterList(this.func)) {
      stFunction.add("params", decl);
      stFunction.add("names", decl.split(" ")[1]);
    }

    if (!ir.getExterns().isEmpty()) {
      for (final Variable extern : ir.getExterns().values()) {
        stFunction.add("members", String.format(
            "private final MmuExternVariable %s;",
            extern.getName()));

        stFunction.add("stmts", String.format(
            "this.%s = new MmuExternVariable(\"%s\", %s, Extern.get().%s);",
            extern.getName(), extern.getName(), extern.getBitSize(), extern.getName()));
      }
      stFunction.add("members", "");
      stFunction.add("stmts", "");
    }

    for (final Variable variable : func.getLocals().values()) {
      final String name = getVariableName(variable.getName());

      stFunction.add("locals", getVarDecl(func.getName(), variable));
      stFunction.add("stmts", STBStruct.getFieldDef(name, variable.getType(), group));
      stFunction.add("stmts", String.format("builder.registerVariable(%s);", name));
    }

    final ControlFlowBuilder controlFlowBuilder = new ControlFlowBuilder(
        ir,
        func.getName(),
        stFunction,
        group,
        stFunction
        );

    controlFlowBuilder.build("START", "STOP", func.getOutput(), func.getBody());
    st.add("members", stFunction);
  }
}
