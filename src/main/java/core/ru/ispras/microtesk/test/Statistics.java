/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

public final class Statistics {
  public static enum Activity {
    /** Initializing and launching the generator */
    LAUNCH,

    /** Parsing the test template */
    PARSING,

    /** Constructing abstract sequences */
    SEQUENCING,

    /** Building concrete sequences (including generating test data) */
    GENERATION,

    /** Simulation (execution) */
    SIMULATION,

    /** Printing assembly code */
    PRINTING
  }

  private final Deque<Pair<Activity, Long>> timerStack;
  private final Map<Activity, Long> timeMetrics;
  private long totalTime;

  private int programs;
  private long programLength;

  private int sequences;
  private long instructions;
  private long traceLength;

  private final long programLengthLimit;
  private final long traceLengthLimit;

  public Statistics(final long programLengthLimit, final long traceLengthLimit) {
    this.timerStack = new ArrayDeque<>();
    this.timeMetrics = new EnumMap<Activity, Long>(Activity.class);
    this.totalTime = 0;

    this.programs = 0;
    this.programLength = 0;

    this.sequences = 0;
    this.instructions = 0;
    this.traceLength = 0;

    this.programLengthLimit = programLengthLimit;
    this.traceLengthLimit = traceLengthLimit;
  }

  public long getTotalTime() {
    return totalTime;
  }

  public String getTotalTimeText() {
    return timeToString(totalTime);
  }

  public long getTimeMetric(final Activity activity) {
    InvariantChecks.checkNotNull(activity);
    return timeMetrics.get(activity);
  }

  public String getTimeMetricAsString(final Activity activity) {
    return timeToString(getTimeMetric(activity));
  }

  public void pushActivity(final Activity activity) {
    InvariantChecks.checkNotNull(activity);
    timerStack.push(new Pair<>(activity, new Date().getTime()));
  }

  public void popActivity() {
    final Pair<Activity, Long> timer = timerStack.pop();
    final long value = new Date().getTime() - timer.second;

    final long metric = timeMetrics.get(timer.first);
    timeMetrics.put(timer.first, metric + value);
  }

  public int getPrograms() {
    return programs;
  }

  public int getSequences() {
    return sequences;
  }

  public long getInstructions() {
    return instructions;
  }

  public boolean isProgramLengthLimitExceeded() {
    return programLength >= programLengthLimit;
  }

  public boolean isTraceLengthLimitExceeded() {
    return traceLength >= traceLengthLimit;
  }

  public void newProgram() {
    programs++;
    programLength = 0;
    traceLength = 0;
  }

  public void newSequence() {
    sequences++;
    traceLength = 0;
  }

  public static String timeToString(long time) {
    final long useconds = time % 1000;
    final long seconds = (time /= 1000) % 60;
    final long minutes = (time /= 60) % 60;
    final long hours = time / 60;

    final StringBuilder sb = new StringBuilder();

    if (hours != 0) {
      sb.append(String.format("%d hours ", hours));
    }

    if (hours != 0 || minutes != 0) {
      sb.append(String.format("%d minutes ", minutes));
    }

    sb.append(String.format("%d.%03d seconds ", seconds, useconds));
    return sb.toString();
  }
}
