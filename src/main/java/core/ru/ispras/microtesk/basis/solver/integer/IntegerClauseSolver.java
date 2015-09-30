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
import java.util.HashSet;
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
  /** Equation clause to be solved (unmodifiable). */
  private final IntegerClause<IntegerVariable> clause;
  /** Variables to be put into a solution (unmodifiable). */
  private final Collection<IntegerVariable> variables;

  /** Variables with their domains (modified by the solver). */
  private final Map<IntegerVariable, IntegerDomain> domains = new LinkedHashMap<>();

  /** Maps a variable {@code x} into the set of variables that are equal to {@code x}. */
  private final Map<IntegerVariable, Set<IntegerVariable>> equalTo = new LinkedHashMap<>();
  /** Maps a variable {@code x} into the set of variables that are not equal to {@code x}. */
  private final Map<IntegerVariable, Set<IntegerVariable>> notEqualTo = new LinkedHashMap<>();

  /**
   * Constructs an equation clause solver.
   * 
   * @param variables the variables to be put into a solution.
   * @param clause the equation clause to be solved.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public IntegerClauseSolver(
      final Collection<IntegerVariable> variables,
      final IntegerClause<IntegerVariable> clause) {
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(clause);

    this.variables = Collections.unmodifiableCollection(variables);
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

  @Override
  public SolverResult<Map<IntegerVariable, BigInteger>> solve(final Mode mode) {
    InvariantChecks.checkNotNull(mode);
    return clause.getType() == IntegerClause.Type.OR ? solveOrClause(mode) : solveAndClause(mode);
  }

  private SolverResult<Map<IntegerVariable, BigInteger>> solveOrClause(final Mode mode) {
    InvariantChecks.checkTrue(clause.getType() == IntegerClause.Type.OR);

    for (final IntegerEquation<IntegerVariable> equation : clause.getEquations()) {
      final IntegerClause<IntegerVariable> variant =
          new IntegerClause<IntegerVariable>(IntegerClause.Type.AND);

      variant.addEquation(equation);

      final IntegerClauseSolver solver =  new IntegerClauseSolver(variables, variant);
      final SolverResult<Map<IntegerVariable, BigInteger>> result = solver.solve(mode);

      if (result.getStatus() == SolverResult.Status.SAT) {
        return result;
      }
    }

    return new SolverResult<>("UNSAT");
  }

  private SolverResult<Map<IntegerVariable, BigInteger>> solveAndClause(final Mode mode) {
    InvariantChecks.checkTrue(clause.getType() == IntegerClause.Type.AND);

    domains.clear();
    equalTo.clear();
    notEqualTo.clear();

    for (final IntegerVariable variable : variables) {
      addVariable(variable);
    }

    for (final IntegerEquation<IntegerVariable> equation : clause.getEquations()) {
      if (!addEquation(equation)) {
        return new SolverResult<>("UNSAT");
      }
    }

    // Returns false if there is a variable whose domain is empty (updates the set of variables).
    if (!checkDomains()) {
      return new SolverResult<>("UNSAT");
    }

    encloseEqualityRelation();

    // Choose one representative from each equivalent classes and perform replacement.
    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : equalTo.entrySet()) {
      final IntegerVariable var = entry.getKey();
      final Set<IntegerVariable> equalVars = entry.getValue();

      if (!handleEqualities(var, equalVars)) {
        return new SolverResult<>("UNSAT");
      }
    }

    return solveInequalities(mode);
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
   * @return {@code false} if the constraint is definitely unsatisfiable; {@code true} otherwise.
   */
  private boolean addEquation(final IntegerEquation<IntegerVariable> equation) {
    InvariantChecks.checkNotNull(equation);

    final IntegerDomain lhsDomain = domains.get(equation.lhs);

    if (equation.value) {
      // Constraint X == C or X != C.
      InvariantChecks.checkNotNull(lhsDomain, "The variable has not been declared");

      if (equation.equal) {
        // Equality (X == C): restricts the domain to the single value.
        lhsDomain.intersect(new IntegerRange(equation.val));
      } else {
        // Inequality (X != C): exclude the value from the domain.
        lhsDomain.exclude(new IntegerRange(equation.val));
      }
    } else {
      final IntegerDomain rhsDomain = domains.get(equation.rhs);

      // Constraint X == Y or X != Y.
      if (equation.lhs.equals(equation.rhs)) {
        if (!equation.equal) {
          lhsDomain.set(IntegerDomain.EMPTY);
        }
      } else {
        InvariantChecks.checkTrue(lhsDomain != null && rhsDomain != null,
            "The variable has not been declared");

        // The equal-to and not-equal-to maps are symmetrical.
        updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.lhs, equation.rhs);
        updateVariableMap((equation.equal ? equalTo : notEqualTo), equation.rhs, equation.lhs);
      }
    }

    return !lhsDomain.isEmpty();
  }

  /**
   * Produces the transitive closure of the equal-to relation.
   * 
   * <p>Actually, it constructs a part of the closure: symmetrical cases are thrown away.</p>
   */
  private void encloseEqualityRelation() {
    final Set<IntegerVariable> cloneVars = new HashSet<>(variables.size());

    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : equalTo.entrySet()) {
      final IntegerVariable var = entry.getKey();

      if (cloneVars.contains(var)) {
        continue;
      }

      final Set<IntegerVariable> allEqualVars = equalTo.get(var);
      InvariantChecks.checkNotNull(allEqualVars);

      Set<IntegerVariable> equalVars = allEqualVars;
      while (!equalVars.isEmpty()) {
        final Set<IntegerVariable> newEqualVars = new LinkedHashSet<>();

        for (final IntegerVariable equalVar : equalVars) {
          final Set<IntegerVariable> nextEqualVars = equalTo.get(equalVar);

          if (nextEqualVars != null) {
            for (final IntegerVariable nextEqualVar : nextEqualVars) {
              if (!allEqualVars.contains(nextEqualVar) && !var.equals(nextEqualVar)) {
                newEqualVars.add(nextEqualVar);
              }
            }
          }
        }

        allEqualVars.addAll(newEqualVars);
        equalVars = newEqualVars;
      }

      cloneVars.addAll(allEqualVars);
    }

    for (final IntegerVariable cloneVar : cloneVars) {
      equalTo.remove(cloneVar);
    }
  }

  private SolverResult<Map<IntegerVariable, BigInteger>> solveInequalities(final Mode mode) {
    // If the set of inequalities is empty, it is satisfiable.
    if (notEqualTo.isEmpty()) {
      return new SolverResult<>(getSolution(mode));
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
      return new SolverResult<>(getSolution(mode));
    }

    // This code needs to be optimized.
    final Iterator<BigInteger> iterator = minDomain.iterator();

    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      final IntegerClauseSolver problem = new IntegerClauseSolver(this);

      // Try some variant.
      InvariantChecks.checkTrue(iterator.hasValue());
      problem.domains.put(minDomainVar, new IntegerDomain(iterator.value()));

      final SolverResult<Map<IntegerVariable, BigInteger>> result = problem.solveInequalities(mode);

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
   * Handles the equalities {@code var == {var1, ..., varN}}.
   * 
   * @param var the variable to be used instead of {@code var1, ..., varN}.
   * @return {@code false} if the equalities are definitely unsatisfiable; {@code true} otherwise.
   */
  private boolean handleEqualities(final IntegerVariable var, final Set<IntegerVariable> equalVars) {
    InvariantChecks.checkNotNull(var);
    InvariantChecks.checkNotNull(equalVars);
    InvariantChecks.checkNotEmpty(equalVars);

    // The trivial equalities, like x == x, are unexpected.
    InvariantChecks.checkFalse(equalVars.contains(var),
        String.format("Unexpected equality %s == %s", var, var));

    final IntegerDomain lhsDomain = domains.get(var);

    for (final IntegerVariable rhs : equalVars) {
      final IntegerDomain rhsDomain = domains.get(rhs);

      // Intersect domains of the variables from the equation.
      lhsDomain.intersect(rhsDomain);

      // The domain intersection is empty, the equation is unsatisfiable.
      if (lhsDomain.isEmpty()) {
        return false;
      }
    }

    return eliminateEquality(var, equalVars);
  }

  /**
   * Handles the inequality {@code lhs != rhs}.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   * @return {@code false} if the inequality is unsatisfiable; {@code true} otherwise.
   */
  private boolean handleInequality(final IntegerVariable lhs, final IntegerVariable rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    final IntegerDomain lhsDomain = domains.get(lhs);
    final IntegerDomain rhsDomain = domains.get(rhs);

    // The trivial equalities, like x != x, are unexpected.
    InvariantChecks.checkFalse(lhs.equals(rhs),
        String.format("Unexpected inequality %s != %s", lhs, rhs));

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
   * Eliminates the equality from the constraint.
   * 
   * @param var the variable to replace with.
   * @param equalVars the variables to be replaced.
   */
  private boolean eliminateEquality(final IntegerVariable var, final Set<IntegerVariable> equalVars) {
    // Replace the variables with the given one.
    for (final Map.Entry<IntegerVariable, Set<IntegerVariable>> entry : notEqualTo.entrySet()) {
      final IntegerVariable lhs = entry.getKey();
      final Set<IntegerVariable> rhsVars = entry.getValue();

      if (!Collections.disjoint(rhsVars, equalVars)) {
        if (lhs.equals(var)) {
          return false;
        }

        // Replacement in the RHS.
        rhsVars.removeAll(equalVars);
        rhsVars.add(var);
      }
    }

    // Replacement in the LHS.
    Set<IntegerVariable> notEqualVars = notEqualTo.get(var);
    if (notEqualVars == null) {
      notEqualVars = new LinkedHashSet<>();
    }

    for (final IntegerVariable rhs : equalVars) {
      final Set<IntegerVariable> rhsNotEqualVars = notEqualTo.get(rhs);

      if (rhsNotEqualVars != null) {
        notEqualVars.addAll(rhsNotEqualVars);
        notEqualTo.remove(rhs);
      }
    }

    if (notEqualVars.contains(var)) {
      return false;
    }

    if (!notEqualVars.isEmpty()) {
      notEqualTo.put(var, notEqualVars);
    }

    return true;
  }

  /**
   * Eliminates the inequality from the constraint.
   * 
   * @param lhs the left-hand-side variable.
   * @param rhs the right-hand-side variable.
   */
  private void eliminateInequality(final IntegerVariable lhs, final IntegerVariable rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

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
      final IntegerVariable lhs, final IntegerVariable rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

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
   * @param mode the solver mode.
   * @return a possible variable-to-value mapping.
   */
  private Map<IntegerVariable, BigInteger> getSolution(final Mode mode) {
    if (mode == Mode.SAT) {
      return Collections.emptyMap();
    }

    final Map<IntegerVariable, BigInteger> solution = new LinkedHashMap<>();

    for (final Map.Entry<IntegerVariable, IntegerDomain> entry : domains.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final IntegerDomain domain = domains.get(variable);
      final Iterator<BigInteger> iterator = domain.iterator();

      iterator.init();
      InvariantChecks.checkTrue(iterator.hasValue());

      domain.set(iterator.value());
      solution.put(variable, iterator.value());

      if (equalTo.containsKey(variable)) {
        for (final IntegerVariable equalVariable : equalTo.get(variable)) {
          final IntegerDomain equalDomain = domains.get(equalVariable);
          equalDomain.intersect(domain);
        }
      }

      if (notEqualTo.containsKey(variable)) {
        for (final IntegerVariable notEqualVariable : notEqualTo.get(variable)) {
          final IntegerDomain notEqualDomain = domains.get(notEqualVariable);
          notEqualDomain.exclude(domain);
        }
      }
    }

    InvariantChecks.checkTrue(solution.size() == domains.size());
    return solution;
  }
}
