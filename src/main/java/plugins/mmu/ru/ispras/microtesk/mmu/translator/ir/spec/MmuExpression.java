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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuExpression} represents an expression, which is a sequence of atoms.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuExpression {
  //------------------------------------------------------------------------------------------------
  // Composed Expressions
  //------------------------------------------------------------------------------------------------

  /**
   * Creates a concatenation of the fields.
   * 
   * @param atoms the fields to be concatenated.
   * @return the expression.
   */
  public static MmuExpression cat(final List<IntegerField> fields) {
    return new MmuExpression(fields);
  }

  public static MmuExpression cat(final IntegerField... fields) {
    return rcat(Arrays.<IntegerField>asList(fields));
  }

  public static MmuExpression catVars(final List<IntegerVariable> variables) {
    final List<IntegerField> fields = new ArrayList<>(variables.size());

    for (final IntegerVariable variable : variables) {
      fields.add(new IntegerField(variable));
    }

    return new MmuExpression(fields);
  }

  public static MmuExpression catVars(final IntegerVariable... variables) {
    return catVars(Arrays.<IntegerVariable>asList(variables));
  }

  /**
   * Creates a reversed concatenation of the fields.
   * 
   * @param atoms the fields to be concatenated.
   * @return the expression.
   */
  public static MmuExpression rcat(final List<IntegerField> fields) {
    final List<IntegerField> reversedAtoms = new ArrayList<>(fields.size());

    for (final IntegerField atom : fields) {
      reversedAtoms.add(0, atom);
    }

    return new MmuExpression(reversedAtoms);
  }

  public static MmuExpression rcat(final IntegerField... fields) {
    return rcat(Arrays.<IntegerField>asList(fields));
  }

  public static MmuExpression rcatVars(final Collection<IntegerVariable> variables) {
    final List<IntegerField> reversedAtoms = new ArrayList<>(variables.size());

    for (final IntegerVariable variable : variables) {
      reversedAtoms.add(0, new IntegerField(variable));
    }

    return new MmuExpression(reversedAtoms);
  }

  public static MmuExpression rcatVars(final IntegerVariable... variables) {
    return rcatVars(Arrays.<IntegerVariable>asList(variables));
  }

  //------------------------------------------------------------------------------------------------
  // Atomic Expressions
  //------------------------------------------------------------------------------------------------

  public static MmuExpression empty() {
    return new MmuExpression();
  }

  public static MmuExpression field(final IntegerField field) {
    return new MmuExpression(field);
  }

  public static MmuExpression var(final IntegerVariable variable) {
    return field(new IntegerField(variable));
  }

  public static MmuExpression var(final IntegerVariable variable, final int lo, final int hi) {
    return field(new IntegerField(variable, lo, hi));
  }

  public static MmuExpression val(final BigInteger value, int width) {
    return var(new IntegerVariable(String.format("%s:%d", value, width), width, value));
  }

  //------------------------------------------------------------------------------------------------
  // Internals
  //------------------------------------------------------------------------------------------------

  private final List<IntegerField> atoms = new ArrayList<>();

  private MmuExpression() {}

  private MmuExpression(final List<IntegerField> atoms) {
    InvariantChecks.checkNotNull(atoms);
    this.atoms.addAll(atoms);
  }

  private MmuExpression(final IntegerField atom) {
    InvariantChecks.checkNotNull(atom);
    this.atoms.add(atom);
  }

  public int size() {
    return atoms.size();
  }
  
  public List<IntegerField> getAtoms() {
    return atoms;
  }

  public int getWidth() {
    int width = 0;

    for (final IntegerField field : atoms) {
      width += field.getWidth();
    }

    return width;
  }

  @Override
  public String toString() {
    if (atoms.isEmpty()) {
      return "empty";
    }

    if (atoms.size() == 1) {
      return atoms.get(0).toString();
    }

    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    boolean comma = false;

    builder.append("{");
    for (final IntegerField field : atoms) {
      builder.append(comma ? separator : "");
      builder.append(field);
      comma = true;
    }
    builder.append("}");

    return builder.toString();
  }
}
