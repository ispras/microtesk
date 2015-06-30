/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.Result;

/**
 * {@link SolverResult} defines result of an {@link Solver}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class SolverResult<T> extends Result<SolverResult.Status, T> {
  public static enum Status {
    SAT,
    UNSAT
  }

  public SolverResult(
      final SolverResult.Status status,
      final T result,
      final List<String> errors) {
    super(status, result, errors);
  }

  public SolverResult(final T result) {
    super(Status.SAT, result, Collections.<String>emptyList());
  }

  public SolverResult(final String error) {
    super(Status.UNSAT, null, Collections.singletonList(error));
  }
}
