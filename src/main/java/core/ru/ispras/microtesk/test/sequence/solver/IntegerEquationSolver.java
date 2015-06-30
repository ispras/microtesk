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

package ru.ispras.microtesk.test.sequence.solver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;

/**
 * This class implements a simple constraint solver. The solver supports equalities and inequalities
 * of variables and constants {@code (x == y, x != y, x == c and x != c)}. A constraint has the
 * following structure: {@code (e[1,1] || ... || e[1,n(1)]) && ... && (e[m,1] || ... || e[m,n(m)])}.
 * It is assumed that the number of non-trivial disjunctions is rather small (i.e., the most of
 * {@code n(i)} are equal to one).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerEquationSolver {
  /** The variables and their domains (the domains are modified by the solver). */
  private Map<IntegerVariable, IntegerDomain> domains = new LinkedHashMap<>();

  /** Maps a variable x into a set of variables that are equal to x. */
  private Map<IntegerVariable, Set<IntegerVariable>> equalTo = new LinkedHashMap<>();
  /** Maps a variable x into a set of variables that are not equal to x. */
  private Map<IntegerVariable, Set<IntegerVariable>> notEqualTo = new LinkedHashMap<>();

  /** The OR-connected subproblems. */
  private List<IntegerEquationSolver> disjunction = new ArrayList<>();

  /**
   * Constructs a solver.
   */
  public IntegerEquationSolver() {
    // Do nothing.
  }

  /**
   * Constructs a copy of the solver.
   * 
   * @param rhs the solver to be copied.
   * @throws NullPointerException if {@code rhs} is null.
   */
  public IntegerEquationSolver(final IntegerEquationSolver rhs) {
    InvariantChecks.checkNotNull(rhs);

    // The objects need to be cloned (their content is modified during constraint solving).
    for (final Map.Entry<IntegerVariable, IntegerDomain> entry : rhs.domains.entrySet()) {
      domains.put(entry.getKey(), new IntegerDomain(entry.getValue()));
    }

    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : rhs.equalTo.entrySet()) {
      equalTo.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
    }

    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : rhs.notEqualTo.entrySet()) {
      notEqualTo.put(entry.getKey(), new LinkedHashSet<>(entry.getValue()));
    }

    for (final IntegerEquationSolver solver : rhs.disjunction) {
      disjunction.add(new IntegerEquationSolver(solver));
    }
  }

  /**
   * Adds the variable. Variables should be added before equations.
   * 
   * @param var the variable to be added.
   * @throws NullPointerException if {@code var} is null.
   */
  public void addVariable(final IntegerVariable var) {
    InvariantChecks.checkNotNull(var);

    if (!disjunction.isEmpty()) {
      for (final IntegerEquationSolver solver : disjunction) {
        solver.addVariable(var);
      }
    } else {
      domains.put(var, new IntegerDomain(var.getWidth()));
    }
  }

  /**
   * Adds the equality {@code lhs == rhs} or inequality {@code lhs != rhs}.
   *  
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @param equal the equality/inequality flag.
   * @throws NullPointerException if {@code lhs} or {@code rhs} is null.
   */
  public void addEquation(final IntegerVariable lhs, final IntegerVariable rhs, final boolean equal) {
    addEquation(new IntegerEquation(lhs, rhs, equal));
  }

  /**
   * Adds the equality {@code var == val} or inequality {@code var != val}.
   *  
   * @param var the left-hand-side variable.
   * @param val the right-hand-side value.
   * @param equal the equality/inequality flag.
   * @throws NullPointerException if {@code var} or {@code val} is null.
   */
  public void addEquation(final IntegerVariable var, final BigInteger val, final boolean equal) {
    addEquation(new IntegerEquation(var, val, equal));
  }

  /**
   * Adds the equation to the constraint.
   * 
   * @param equation the equation to be added.
   * @throws NullPointerException if {@code equation} is null.
   */
  public void addEquation(final IntegerEquation equation) {
    InvariantChecks.checkNotNull(equation);

    if (!disjunction.isEmpty()) {
      for (final IntegerEquationSolver solver : disjunction) {
        solver.addEquation(equation);
      }
    } else {
      if (equation.value) {
        final IntegerDomain domain = domains.get(equation.lhs);

        if (domain == null) {
          throw new IllegalStateException("The variable has not been declared");
        }

        if (equation.equal) {
          // Equality (var == val): restricts the domain to the single value.
          domain.intersect(new IntegerRange(equation.val));
        } else {
          // Inequality (var != val): exclude the value from the domain.
          domain.exclude(new IntegerRange(equation.val));
        }
      } else {
        if (equation.lhs.equals(equation.rhs)) {
          throw new IllegalArgumentException("The LHS variable is equal to the RHS variable");
        }

        if (!domains.containsKey(equation.lhs) || !domains.containsKey(equation.rhs)) {
          throw new IllegalStateException("The variable has not been declared");
        }

        // The equal-to and not-equal-to maps are symmetrical.
        updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.lhs, equation.rhs);
        updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.rhs, equation.lhs);
      }
    }
  }

  /**
   * Adds the equation set.
   * 
   * @param equationSet the equation set to be added.
   * @throws NullPointerException if {@code equationSet} is null.
   */
  public void addEquationSet(final IntegerEquationSet equationSet) {
    InvariantChecks.checkNotNull(equationSet);

    if (!disjunction.isEmpty()) {
      for (final IntegerEquationSolver solver : disjunction) {
        solver.addEquationSet(equationSet);
      }
    } else {
      if (equationSet.getType() == IntegerEquationSet.Type.OR && equationSet.size() > 1) {
        final List<IntegerEquationSolver> problems = new ArrayList<>(equationSet.size());

        for (final IntegerEquation equation : equationSet.getEquations()) {
          final IntegerEquationSolver problem = new IntegerEquationSolver(this);

          problem.addEquation(equation);
          problems.add(problem);
        }

        domains.clear();
        equalTo.clear();
        notEqualTo.clear();

        disjunction.addAll(problems);
      } else {
        for (final IntegerEquation equation : equationSet.getEquations()) {
          addEquation(equation);
        }
      }
    }
  }

  /**
   * Checks whether the constraint (a set of equalities/inequalities) is satisfiable.
   * 
   * @return {@code true} if the constraint is satisfiable; {@code false} otherwise.
   */
  public boolean solve() {
    // The constraint to be solved is a disjunction of sub-constraints.
    if (!disjunction.isEmpty()) {
      for (final IntegerEquationSolver solver : disjunction) {
        if (solver.solve()) {
          return true;
        }
      }

      return false;
    }

    // Returns false if there is a variable whose domain is empty (updates the set of variables).
    if (!checkDomains()) {
      return false;
    }

    final Set<IntegerVariable> variables = new LinkedHashSet<>(equalTo.keySet());

    // Eliminate all equalities.
    for (final IntegerVariable lhs : variables) {
      final Set<IntegerVariable> equalVars = equalTo.get(lhs);
      final Set<IntegerVariable> notEqualVars = notEqualTo.get(lhs);

      if (equalVars != null) {
        if (notEqualVars != null) {
          // There is a conflict: x == y && x != y.
          if (!Collections.disjoint(equalVars, notEqualVars)) {
            return false;
          }
        }

        // Choose some variable that is equal to the current one.
        if (!equalVars.isEmpty()) {
          final IntegerVariable rhs = equalVars.iterator().next();

          if (!handleEquality(lhs, rhs)) {
            return false;
          }
        }
      }
    }

    // Debug check.
    if (!equalTo.isEmpty()) {
      throw new IllegalStateException(
          String.format("The set of equalities has not been reduced: %s", this));
    }

    return solveInequalities();
  }

  /**
   * Checks whether the set of inequalities is satisfiable.
   * 
   * @param equations the set of inequalities.
   * @return {@code true} if the constraint is satisfiable; {@code false} otherwise.
   */
  private boolean solveInequalities() {
    // If the set of inequalities is empty, it is satisfiable.
    if (notEqualTo.isEmpty()) {
      return true;
    }

    // Returns false if there is a variable whose domain is empty (updates the set of variables).
    if (!checkDomains()) {
      return false;
    }

    final Set<IntegerVariable> variables = new LinkedHashSet<>(notEqualTo.keySet());

    // Solve inequalities.
    for (final IntegerVariable lhs : variables) {
      final Set<IntegerVariable> notEqualVars = notEqualTo.get(lhs);

      if (notEqualVars != null) {
        // Choose some variable that is equal to the current one. 
        final Set<IntegerVariable> rhsVars = new LinkedHashSet<>(notEqualVars);

        for (final IntegerVariable rhs : rhsVars) {
          if (!handleInequality(lhs, rhs)) {
            return false;
          }
        }
      }
    }

    // If for each variable x, dom(x) has more values than the number of variables that are not
    // equal to x, the constraint is obviously satisfiable.
    boolean simple = true;

    IntegerDomain minDomain = null;
    IntegerVariable minDomainVar = null;

    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : notEqualTo.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final IntegerDomain domain = domains.get(variable);
      final Set<IntegerVariable> notEqualVars = entry.getValue();

      if (domain.size().compareTo(BigInteger.valueOf(notEqualVars.size())) <= 0) {
        simple = false;

        if (minDomain == null || domain.size().compareTo(minDomain.size()) < 0) {
          minDomain = domain;
          minDomainVar = variable;
        }
      }
    }

    if (simple) {
      return true;
    }

    // This code needs to be optimized.
    final Iterator<BigInteger> iterator = minDomain.iterator();

    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      final IntegerEquationSolver problem = new IntegerEquationSolver(this);

      // Try some variant.
      problem.domains.put(minDomainVar, new IntegerDomain(iterator.value()));

      if (problem.solveInequalities()) {
        return true;
      }
    }

    return false;
  }
  
  /**
   * Checks whether there is no variables with the empty domain.
   * 
   * @return {@code false} if there is a variable whose domain is empty; {@code true} otherwise.
   */
  private boolean checkDomains() {
    for (final IntegerDomain domain : domains.values()) {
      if (domain.isEmpty()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Handles the equality {@code lhs == rhs}.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @return {@code false} if the equality is unsatisfiable; {@code true} otherwise.
   */
  private boolean handleEquality(final IntegerVariable lhs, final IntegerVariable rhs) {
    final IntegerDomain lhsDomain = domains.get(lhs);
    final IntegerDomain rhsDomain = domains.get(rhs);

    // The trivial equalities, like x == x, are unexpected.
    if (lhs.equals(rhs)) {
      throw new IllegalStateException(String.format("Unexpected equality %s == %s", lhs, rhs));
    }

    // Intersect domains of the variables from the equation.
    lhsDomain.intersect(rhsDomain);

    // The domain intersection is empty, the equation is unsatisfiable.
    if (lhsDomain.isEmpty()) {
      return false;
    }

    // Update the equal-to sets.
    final Set<IntegerVariable> lhsEqualVars = equalTo.get(lhs);

    // Update the domains of the variables that are equal to the LHS variable.
    // The domain of the LHS variable has been already updated.
    for (final IntegerVariable var : lhsEqualVars) {
      domains.get(var).set(lhsDomain);
    }

    // Replace the LHS variable with the RHS one.
    eliminateEquality(lhs, rhs);

    return true;
  }

  /**
   * Handles the inequality {@code lhs != rhs}.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @return {@code false} if the inequality is unsatisfiable; {@code true} otherwise.
   */
  private boolean handleInequality(final IntegerVariable lhs, final IntegerVariable rhs) {
    final IntegerDomain lhsDomain = domains.get(lhs);
    final IntegerDomain rhsDomain = domains.get(rhs);

    // The trivial equalities, like x == x, are unexpected.
    if (lhs.equals(rhs)) {
      throw new IllegalStateException(String.format("Unexpected inequality %s != %s", lhs, rhs));
    }

    // If the variables' domains are disjoint, the inequality is redundant.
    if (!lhsDomain.overlaps(rhsDomain)) {
      eliminateInequality(lhs, rhs);
      return true;
    }

    // If the domains contains only one common value, that value is removed from the domains,
    // while the inequality is removed from the constraint.
    final IntegerDomain common = new IntegerDomain(lhsDomain);
    common.intersect(rhsDomain);

    if (common.isSingular()) {
      lhsDomain.exclude(common);
      rhsDomain.exclude(common);

      if (lhsDomain.isEmpty() || rhsDomain.isEmpty()) {
        return false;
      }

      eliminateInequality(lhs, rhs);
      return true;
    }

    return true;
  }

  /**
   * Replaces all occurrences of the variable {@code lhs} with the variable {@code rhs} and
   * eliminates the variable {@code lhs} from the equations.
   * 
   * @param lhs the left-hand-side variable (the variable to be replaced).
   * @param rhs the right-hand-side variable (the variable to replace with).
   */
  private void eliminateEquality(final IntegerVariable lhs, final IntegerVariable rhs) {
    // Handle the equal-to map.
    Set<IntegerVariable> lhsEqualVars = equalTo.get(lhs);
    Set<IntegerVariable> rhsEqualVars = equalTo.get(rhs);

    // This call should proceed the next one not to include the variable to its equal-to set.
    lhsEqualVars.remove(rhs);

    // If LHS = RHS and LHS is equal to {x1, ..., xN}, then RHS is equal to {x1, ..., xN}. 
    rhsEqualVars.addAll(lhsEqualVars);
    // Remove the LHS variable (the variable being excluded).
    rhsEqualVars.remove(lhs);

    // Replace all occurrences of the LHS variable in the equal-to map with the RHS variable.
    for (final IntegerVariable var : lhsEqualVars) {
      final Set<IntegerVariable> equalVars = equalTo.get(var);
      equalVars.remove(lhs);
      equalVars.add(rhs);
    }

    // Exclude the LHS variable from the equalities.
    equalTo.remove(lhs);
    // If the equal-to set of the RHS variable is empty, the RHS variable is excluded as well.
    if (rhsEqualVars.isEmpty()) {
      equalTo.remove(rhs);
    }

    // Handle the not-equal-to map.
    Set<IntegerVariable> lhsNotEqualVars = notEqualTo.get(lhs);
    Set<IntegerVariable> rhsNotEqualVars = notEqualTo.get(rhs);

    // There are inequalities that contain the LHS variable.
    if (lhsNotEqualVars != null) {
      // It is not guaranteed that there are inequalities that contain the RHS variable.
      if (rhsNotEqualVars == null) {
        notEqualTo.put(rhs, rhsNotEqualVars = new LinkedHashSet<>());
      }

      // If LHS = RHS and LHS is not equal to {x1, ..., xN}, then RHS is not equal to {x1, ..., xN}.
      rhsNotEqualVars.addAll(lhsNotEqualVars);
      // Remove the LHS variable (the variable being excluded).
      rhsNotEqualVars.remove(lhs);

      // Replace all occurrences of the LHS variable in the not-equal-to map with the RHS variable.
      for (final IntegerVariable var : lhsNotEqualVars) {
        final Set<IntegerVariable> notEqualVars = notEqualTo.get(var);
        notEqualVars.remove(lhs);
        notEqualVars.add(rhs);
      }

      // Exclude the variable from the inequalities.
      notEqualTo.remove(lhs);
      // If the not-equal-to set of the RHS variable is empty, the RHS variable is excluded as well.
      if (rhsNotEqualVars != null && rhsNotEqualVars.isEmpty()) {
        notEqualTo.remove(rhs);
      }
    }
  }

  /**
   * Eliminates the inequality from the constraint.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   */
  private void eliminateInequality(final IntegerVariable lhs, final IntegerVariable rhs) {
    final Set<IntegerVariable> lhsNotEqualVars = notEqualTo.get(lhs);
    final Set<IntegerVariable> rhsNotEqualVars = notEqualTo.get(rhs);

    lhsNotEqualVars.remove(rhs);
    if (lhsNotEqualVars.isEmpty()) {
      notEqualTo.remove(lhs);
    }

    rhsNotEqualVars.remove(lhs);
    if (rhsNotEqualVars.isEmpty()) {
      notEqualTo.remove(rhs);
    }
  }

  /**
   * Puts {@code rhs} into the equal-to/not-equal-to set of {@code lhs}.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   */
  private static void updateVariableMap(final Map<IntegerVariable, Set<IntegerVariable>> map,
      final IntegerVariable lhs, IntegerVariable rhs) {
    Set<IntegerVariable> vars = map.get(lhs);

    if (vars == null) {
      map.put(lhs, vars = new LinkedHashSet<>());
    }

    vars.add(rhs);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    if (!disjunction.isEmpty()) {
      builder.append("OR {\n");

      for (final IntegerEquationSolver solver : disjunction) {
        builder.append(solver);
      }

      builder.append("}\n");
    } else {
      builder.append("AND {\n");

      for (final IntegerVariable lhs : equalTo.keySet()) {
        for (final IntegerVariable rhs : equalTo.get(lhs)) {
          builder.append(String.format("%s == %s\n", lhs, rhs));
        }
      }
  
      for (final IntegerVariable lhs : notEqualTo.keySet()) {
        for (final IntegerVariable rhs : notEqualTo.get(lhs)) {
          builder.append(String.format("%s != %s\n", lhs, rhs));
        }
      }

      builder.append("}\n");
    }

    return builder.toString();
  }
}
