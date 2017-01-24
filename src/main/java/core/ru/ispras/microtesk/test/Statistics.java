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
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

/**
 * The {@link Statistics} class collects statistical information and performance metrics
 * during test program generation.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Statistics {
  public static enum Activity {
    INITIALIZING("Initialization time"),
    PARSING     ("Template parsing time"),
    SEQUENCING  ("Sequence construction time"),
    PROCESSING  ("Sequence concretization time"),
    SIMULATING  ("Simulation time"),
    PRINTING    ("Printing time");

    private final String text;

    private Activity(final String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }
  }

  private final Deque<Pair<Activity, Long>> activities;
  private final Map<Activity, Long> timeMetrics;
  private final long startTime;
  private long totalTime;

  private int programs;
  private long programLength;

  private int sequences;
  private long instructions;
  private long totalTraceLength;
  private long sequenceTraceLength;

  private long programLengthLimit;
  private long traceLengthLimit;

  public Statistics() {
    this(0L, 0L);
  }

  public Statistics(final long programLengthLimit, final long traceLengthLimit) {
    this.activities = new ArrayDeque<>();
    this.timeMetrics = new EnumMap<>(Activity.class);

    this.startTime = getCurrentTime();
    this.totalTime = 0;

    for (final Activity activity : Activity.values()) {
      this.timeMetrics.put(activity, 0L);
    }
 
    this.programs = 0;
    this.programLength = 0;

    this.sequences = 0;
    this.instructions = 0;
    this.totalTraceLength = 0;
    this.sequenceTraceLength = 0;

    this.programLengthLimit = programLengthLimit;
    this.traceLengthLimit = traceLengthLimit;
  }

  private static long getCurrentTime() {
    return System.currentTimeMillis();
  }

  public void saveTotalTime() {
    while (!activities.isEmpty()) {
      popActivity();
    }

    this.totalTime = getCurrentTime() - startTime;
  }

  public void pushActivity(final Activity activity) {
    InvariantChecks.checkNotNull(activity);
    activities.push(new Pair<>(activity, getCurrentTime()));
  }

  public void popActivity() {
    final Pair<Activity, Long> activity = activities.pop();
    final long value = getCurrentTime() - activity.second;

    // Increases metric associated with current activity
    addToTimeMetric(activity.first, value);

    // Decreases metric associated with parent activity to exclude time of current activity
    if (!activities.isEmpty()) {
      addToTimeMetric(activities.peek().first, -value);
    }
  }

  private void addToTimeMetric(final Activity activity, final long value) {
    final long metric = timeMetrics.get(activity);
    timeMetrics.put(activity, metric + value);
  }

  public void incPrograms() {
    programs++;
    programLength = 0;
    sequenceTraceLength = 0;
  }

  public void decPrograms() {
    programs--;
    programLength = 0;
  }

  public void incSequences() {
    sequences++;
    sequenceTraceLength = 0;
  }

  public void incInstructions() {
    incInstructions(1);
  }

  public void incInstructions(final int count) {
    instructions += count;
    programLength += count;
  }

  public void incTraceLength() {
    totalTraceLength++;
    sequenceTraceLength++;
  }

  public long getTotalTime() {
    if (0 == totalTime) {
      saveTotalTime();
    }

    return totalTime;
  }

  public long getTimeMetric(final Activity activity) {
    InvariantChecks.checkNotNull(activity);
    return timeMetrics.get(activity);
  }

  public String getTimeMetricText(final Activity activity) {
    final long metric = getTimeMetric(activity);
    final long percentage = (metric * 10000) / getTotalTime();
    return String.format("%s: %s (%d.%d%%)",
        activity.getText(), timeToString(metric), percentage / 100, percentage % 100);
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

  public long getProgramLength() {
    return programLength;
  }

  public long getTotalTraceLength() {
    return totalTraceLength;
  }

  public boolean isFileLengthLimitExceeded() {
    return isProgramLengthLimitExceeded() || isTraceLengthLimitExceeded();
  }

  public boolean isProgramLengthLimitExceeded() {
    return programLength >= programLengthLimit;
  }

  public boolean isTraceLengthLimitExceeded() {
    return sequenceTraceLength >= traceLengthLimit;
  }

  public void setProgramLengthLimit(final long value) {
    this.programLengthLimit = value;
  }

  public void setTraceLengthLimit(final long value) {
    this.traceLengthLimit = value;
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

    sb.append(String.format("%d.%03d seconds", seconds, useconds));
    return sb.toString();
  }
}
