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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class represents a result of a constraint solver.
 * 
 * @param <T> solution type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SolverResult<T> {
  /**
   * This enumeration contains solver statuses.
   */
  public static enum Status {
    /** The constraint is satisfiable (the solver has solved it). */
    SAT,
    /** The constraint is unsatisfiable (the solver can solve it). */
    UNSAT,
    /** It is unknown whether the constraint is satisfiable (the solver has failed to solve it). */
    UNKNOWN
  }

  /** The solver status. */
  private Status status;
  /** The solution (if {@code status} is {@code SAT}). */
  private T solution;
  /** The warning or error message. */
  private String message;

  /**
   * Constructs a solver result (SAT/UNSAT).
   * 
   * @param status the solver status.
   * @param solution the solution.
   * @param message the message.
   * @throws IllegalArgumentException if {@code status == null}.
   */
  public SolverResult(final Status status, final T solution, final String message) {
    InvariantChecks.checkNotNull(status);

    this.status = status;
    this.solution = solution;
    this.message = message;
  }

  /**
   * Constructs a solver result (SAT/UNSAT).
   * 
   * @param status the solver status.
   * @param solution the solution.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public SolverResult(final Status status, final T solution) {
    this(status, solution, null);
  }

  /**
   * Constructs a solver result (SAT/UNSAT).
   * 
   * @param status the solver status.
   * @throws IllegalArgumentException if {@code status} is null.
   */
  public SolverResult(final Status status) {
    this(status, null, null);
  }

  /**
   * Constructs a solver result (SAT).
   * 
   * @param solution the solution.
   * @throws IllegalArgumentException if {@code solution} is null.
   */
  public SolverResult(final T solution) {
    this(Status.SAT, solution, null);
  }

  /**
   * Constructs a solver result (UNSAT).
   * 
   * @param message the message.
   * @throws IllegalArgumentException if {@code message} is null.
   */
  public SolverResult(final String message) {
    this(Status.UNSAT, null, message);
  }

  /**
   * Constructs a solver result (UNSAT or UNKNOWN).
   * 
   * @param status the solver status.
   * @param message the solution.
   * @throws IllegalArgumentException if {@code status} or {@code message} is null.
   */
  public SolverResult(final Status status, final String message) {
    this(status, null, message);
  }

  /**
   * Returns the solver status.
   * 
   * @return the solver status.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Returns the solution (generated data).
   * 
   * @return the solution.
   */
  public T getSolution() {
    return solution;
  }

  /**
   * Returns the error or warning message.
   * 
   * @return the message.
   */
  public String getMessage() {
    return message;
  }
}
