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

import java.util.Date;

/**
 * The {@code TestStatistics} class stores test generation statistics.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class TestStatistics {
  public final Date startTime;
  public long instructionCount;
  public long instructionExecutedCount;
  public int testCaseNumber;
  public int testProgramNumber;

  public TestStatistics() {
    this(new Date(), 0, 0, 0, 0);
  }

  private TestStatistics(final TestStatistics other) {
    this(
        other.startTime,
        other.instructionCount,
        other.instructionExecutedCount,
        other.testCaseNumber,
        other.testProgramNumber
    );
  }

  private TestStatistics(
      final Date startTime,
      final long instructionCount,
      final long instructionExecutedCount,
      final int testCaseNumber,
      final int testProgramNumber) {
    this.startTime = startTime;
    this.instructionCount = instructionCount;
    this.instructionExecutedCount = instructionExecutedCount;
    this.testCaseNumber = testCaseNumber;
    this.testProgramNumber = testProgramNumber;
  }

  public TestStatistics copy() {
    return new TestStatistics(this);
  }

  @Override
  public String toString() {
    return String.format(
       "%s [start=%d, instructions=%d, instructionExecuted=%d, testCases=%d, testPrograms=%d]",
       getClass().getSimpleName(),
       startTime.getTime(),
       instructionCount,
       instructionExecutedCount,
       testCaseNumber,
       testProgramNumber
       );
  }
}
