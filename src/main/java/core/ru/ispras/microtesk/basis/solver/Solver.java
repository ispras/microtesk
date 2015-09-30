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

package ru.ispras.microtesk.basis.solver;

/**
 * {@link Solver} defines an interface of solvers.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Solver<T> {
  /**
   * {@link Mode} represents a solver mode. 
   */
  public static enum Mode {
    /** SAT/UNSAT. */
    SAT,
    /** (SAT, MAP)/UNSAT. */ 
    MAP
  };

  /**
   * Checks whether the equation clause is satisfiable and returns a solution (if required).
   * 
   * @param the solver mode.
   * @return {@code SAT} if the equation clause is satisfiable; {@code UNSAT} otherwise.
   */
  SolverResult<T> solve(Mode mode);
}
