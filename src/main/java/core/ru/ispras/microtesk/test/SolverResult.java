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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.Result;
import ru.ispras.microtesk.test.sequence.iterator.Iterator;

import java.util.List;

/**
 * {@link SolverResult} defines result of a {@link Solver}.
 * 
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
 */

public final class SolverResult<T> extends Result<SolverResult.Status, Iterator<T>> {
  public static enum Status {
    OK,
    ERROR
  }

  public SolverResult(final SolverResult.Status status,
                      final Iterator<T> result,
                      final List<String> errors) {
    super(status, result, errors);
  }
}
