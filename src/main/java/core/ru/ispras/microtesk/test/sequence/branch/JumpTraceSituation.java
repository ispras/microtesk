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
 * {@link JumpTraceSituation} is a base class for unconditional branch situations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class JumpTraceSituation extends BranchTraceSituation {

  public JumpTraceSituation(
      final int branchNumber,
      final int branchIndex,
      final int branchLabel,
      final BranchTrace branchTrace) {
    super(branchNumber, branchIndex, branchLabel, branchTrace, null, null);
  }

  public JumpTraceSituation() {
  }

  protected JumpTraceSituation(final JumpTraceSituation r) {
    super(r);
  }

  @Override
  public void satisfyCondition() {}

  @Override
  public void violateCondition() {}

  @Override
  public Sequence<Call> step() {
    return new Sequence<Call>();
  }

  @Override
  public abstract Sequence<Call> prepare();
}
