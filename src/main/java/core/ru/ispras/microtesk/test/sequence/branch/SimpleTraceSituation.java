/*
 * Copyright 2009-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.branch;

import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Call;

/**
 * {@link SimpleTraceSituation} implements a simple situation for branch instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SimpleTraceSituation extends BranchTraceSituation {
  public SimpleTraceSituation() {}

  protected SimpleTraceSituation(final SimpleTraceSituation r) {
    super(r);
  }

  /**
   * Returns a possibility of using a single-instruction step for the given trace.
   * 
   * @return {@code true} if it is possible to use a simple step; {@code false} otherwise.
   */
  public abstract boolean canUseSimpleStep();

  public abstract Sequence<Call> simpleStep();

  public abstract Sequence<Call> generalStep();

  @Override
  public final Sequence<Call> step() {
    if (branchTrace.getChangeNumber() == 0) {
      return new Sequence<Call>();
    }

    if (canUseSimpleStep()) {
      return simpleStep();
    }

    return generalStep();
  }

  /**
   * Returns a basic preparation program (if a step is absent).
   * 
   * @return a preparation program.
   */
  public abstract Sequence<Call> basicPrepare();

  /**
   * Returns a simple preparation program (if a step is simple).
   * 
   * @return a preparation program.
   */
  public abstract Sequence<Call> simplePrepare();

  /**
   * Returns a general preparation program (if a step is general).
   * 
   * @return a general preparation program.
   */
  public abstract Sequence<Call> generalPrepare();

  @Override
  public final Sequence<Call> prepare() {
    if (branchTrace.isEmpty()) {
      return new Sequence<Call>();
    }

    if (branchTrace.getChangeNumber() == 0) {
      return basicPrepare();
    }

    if (canUseSimpleStep()) {
      return simplePrepare();
    }

    return generalPrepare();
  }
}
