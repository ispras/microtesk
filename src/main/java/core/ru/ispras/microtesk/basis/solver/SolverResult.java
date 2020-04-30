/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.List;
import ru.ispras.fortress.util.Result;

/**
 * {@link SolverResult} defines result of an {@link Solver}.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SolverResult<T> extends Result<SolverResult.Status, T> {

  public enum Status {
    SAT,
    UNSAT,
    UNDEF
  }

  public static <T> SolverResult<T> newSat() {
    return new SolverResult<>(Status.SAT, null, Collections.emptyList());
  }

  public static <T> SolverResult<T> newSat(final T result) {
    return new SolverResult<>(Status.SAT, result, Collections.emptyList());
  }

  public static <T> SolverResult<T> newUnsat() {
    return new SolverResult<>(Status.UNSAT, null, Collections.emptyList());
  }

  public static <T> SolverResult<T> newUnsat(final String error) {
    return new SolverResult<>(Status.UNSAT, null, Collections.singletonList(error));
  }

  public static <T> SolverResult<T> newUndef() {
    return new SolverResult<>(Status.UNDEF, null, Collections.emptyList());
  }

  public static <T> SolverResult<T> newUndef(final String error) {
    return new SolverResult<>(Status.UNDEF, null, Collections.singletonList(error));
  }

  public SolverResult(
      final SolverResult.Status status,
      final T result,
      final List<String> errors) {
    super(status, result, errors);
  }
}
