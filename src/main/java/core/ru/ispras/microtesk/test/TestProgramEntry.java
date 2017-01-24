/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

final class TestProgramEntry {
  private final String sequenceId;
  private final TestSequence sequence;

  public TestProgramEntry(
      final String sequenceId,
      final TestSequence sequence) {
    InvariantChecks.checkNotNull(sequenceId);
    InvariantChecks.checkNotNull(sequence);

    this.sequenceId = sequenceId;
    this.sequence = sequence;
  }

  public String getSequenceId() {
    return sequenceId;
  }

  public TestSequence getSequence() {
    return sequence;
  }
}
