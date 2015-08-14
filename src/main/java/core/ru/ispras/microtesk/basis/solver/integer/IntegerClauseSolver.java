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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link IntegerClauseSolver} implements an equation clause solver.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerClauseSolver implements Solver<Map<IntegerVariable, BigInteger>> {
  /** Equation clause to be solved. */
  private final IntegerClause clause;
  /** Variables used in the clause. */
  private final Collection<IntegerVariable> variables;

  /** Variables with their domains (the domains are modified by the solver). */
  private final Map<IntegerVariable, IntegerDomain> domains = new LinkedHashMap<>();

  /** Maps a variable {@code x} into a set of variables that are equal to {@code x}. */
  private final Map<IntegerVariable, Set<IntegerVariable>> equalTo = new LinkedHashMap<>();
  /** Maps a variable {@code x} into a set of variables that are not equal to {@code x}. */
  private final Map<IntegerVariable, Set<IntegerVariable>> notEqualTo = new LinkedHashMap<>();

  /**
   * Constructs an equation clause solver.
   * 
   * @param variables the variables.
   * @param clause the equation clause to be solved.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public IntegerClauseSolver(
      final Collection<IntegerVariable> variables, final IntegerClause clause) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(clause);

    this.variables = variables;
    this.clause = clause;
  }

  /**
   * Constructs a copy of the solver.
   * 
   * @param rhs the solver to be copied.
   * @throws IllegalArgumentException if {@code rhs} is null.
   */
  public IntegerClauseSolver(final IntegerClauseSolver rhs) {
    InvariantChecks.checkNotNull(rhs);

    variables = rhs.variables;
    clause = rhs.clause;

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
  }

  /**
   * Checks whether the equation clause is satisfiable.
   * 
   * @return {@code true} if the equation clause is satisfiable; {@code false} otherwise.
   */
  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve() {
    // Handle the OR case.
    if (clause.getType() == IntegerClause.Type.OR) {
      for (final IntegerEquation equation : clause.getEquations()) {
        final IntegerClause variant = new IntegerClause(IntegerClause.Type.AND);

        variant.addEquation(equation);

        final IntegerClauseSolver solver =  new IntegerClauseSolver(variables, variant);
        final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve();

        if (result.getStatus() == SolverResult.Status.SAT) {
          return result;
        }
      }

      return new SolverResult<>("UNSAT");
    }

    // Handle the AND case.
    domains.clear();
    equalTo.clear();
    notEqualTo.clear();

    for (final IntegerVariable variable : variables) {
      addVariable(variable);
    }
    for (final IntegerEquation equation : clause.getEquations()) {
      addEquation(equation);
    }

    // Returns false if there is a variable whose domain is empty (updates the set of variables).
    if (!checkDomains()) {
      return new SolverResult<>("UNSAT");
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
            return new SolverResult<>("UNSAT");
          }
        }

        // Choose some variable that is equal to the current one.
        if (!equalVars.isEmpty()) {
          final IntegerVariable rhs = equalVars.iterator().next();

          if (!handleEquality(lhs, rhs)) {
            return new SolverResult<>("UNSAT");
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
   * Adds the variable and constructs its domain in compliance with the bit width.
   * 
   * @param variable the variable to be added.
   */
  private void addVariable(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);

    if (variable.isDefined()) {
      domains.put(variable, new IntegerDomain(variable.getValue()));
    } else {
      domains.put(variable, new IntegerDomain(variable.getWidth()));
    }
  }

  /**
   * Adds the equation to the constraint.
   * 
   * @param equation the equation to be added.
   */
  private void addEquation(final IntegerEquation equation) {
    InvariantChecks.checkNotNull(equation);

    if (equation.value) {
      // Constraint X == C or X != C.
      final IntegerDomain lhsDomain = domains.get(equation.lhs);

      if (lhsDomain == null) {
        throw new IllegalStateException("The variable has not been declared");
      }

      if (equation.equal) {
        // Equality (X == C): restricts the domain to the single value.
        lhsDomain.intersect(new IntegerRange(equation.val));
      } else {
        // Inequality (X != C): exclude the value from the domain.
        lhsDomain.exclude(new IntegerRange(equation.val));
      }
    } else {
      // Constraint X == Y or X != Y.
      if (equation.lhs.equals(equation.rhs)) {
        throw new IllegalArgumentException("The LHS variable is equal to the RHS variable");
      }

      final IntegerDomain lhsDomain = domains.get(equation.lhs);
      final IntegerDomain rhsDomain = domains.get(equation.rhs);

      if (lhsDomain == null || rhsDomain == null) {
        throw new IllegalStateException("The variable has not been declared");
      }

      // The equal-to and not-equal-to maps are symmetrical.
      updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.lhs, equation.rhs);
      updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.rhs, equation.lhs);
    }
  }

  /**
   * Checks whether the set of inequalities is satisfiable.
   * 
   * @param equations the set of inequalities.
   * @return {@code true} if the constraint is satisfiable; {@code false} otherwise.
   */
  private SolverResult<Map<IntegerVariable, BigInteger>> solveInequalities() {
    // If the set of inequalities is empty, it is satisfiable.
    if (notEqualTo.isEmpty()) {
      return new SolverResult<>(getSolution());
    }

    // Returns false if there is a variable whose domain is empty (updates the set of variables).
    if (!checkDomains()) {
      return new SolverResult<>("Empty domain");
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
            return new SolverResult<>("Contradiction found");
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
      return new SolverResult<>(getSolution());
    }

    // This code needs to be optimized.
    final Iterator<BigInteger> iterator = minDomain.iterator();

    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      final IntegerClauseSolver problem = new IntegerClauseSolver(this);

      // Try some variant.
      problem.domains.put(minDomainVar, new IntegerDomain(iterator.value()));

      final SolverResult<Map<IntegerVariable, BigInteger>> result = problem.solveInequalities();

      if (result.getStatus() == SolverResult.Status.SAT) {
        return result;
      }
    }

    return new SolverResult<>("UNSAT");
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

  /**
   * Returns a possible solution (a variable-to-value map).
   * 
   * TODO: Randomization is required.
   * 
   * @return a possible variable-to-value mapping.
   */
  private Map<IntegerVariable, BigInteger> getSolution() {
    final Map<IntegerVariable, BigInteger> solution = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, IntegerDomain> entry : domains.entrySet()) {
      final IntegerVariable lhsVar = entry.getKey();
      final IntegerDomain lhsDomain = entry.getValue();

      if (equalTo.containsKey(lhsVar)) {
        for (final IntegerVariable rhsVar : equalTo.get(lhsVar)) {
          final IntegerDomain rhsDomain = domains.get(rhsVar);
          lhsDomain.intersect(rhsDomain);
        }
      }

      final Iterator<BigInteger> lhsIterator = lhsDomain.iterator();

      lhsIterator.init();
      InvariantChecks.checkTrue(lhsIterator.hasValue());

      lhsDomain.set(lhsIterator.value());

      if (notEqualTo.containsKey(lhsVar)) {
        for (final IntegerVariable rhsVar : notEqualTo.get(lhsVar)) {
          final IntegerDomain rhsDomain = domains.get(rhsVar);
          rhsDomain.exclude(lhsDomain);
        }
      }
    }

    for (final Map.Entry<IntegerVariable, IntegerDomain> entry : domains.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final IntegerDomain domain = entry.getValue();

      final Iterator<BigInteger> iterator = domain.iterator();

      iterator.init();
      InvariantChecks.checkTrue(iterator.hasValue());

      solution.put(variable, iterator.value());
    }

    InvariantChecks.checkTrue(solution.size() == domains.size());
    return solution;
  }
}
