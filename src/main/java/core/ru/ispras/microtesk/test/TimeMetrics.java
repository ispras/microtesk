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

import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class TimeMetrics {
  public static enum Kind {
    /** Time in milliseconds spent on initializing and launching the generator */
    LAUNCH,

    /** Time in milliseconds spent on parsing the test template */
    PARSING,

    /** Time in milliseconds spent on constructing abstract sequences */
    SEQUENCING,

    /** Time in milliseconds spent on spent on building concrete sequences (including generating
     * test data)
     */
    GENERATION,

    /** Time in milliseconds spent on simulation (execution) */
    SIMULATION,

    /** Time in milliseconds spent on printing assembly code */
    PRINTING
  }

  private long total;
  private Map<Kind, Long> metrics;

  public TimeMetrics() {
    total = 0L;
    metrics = new EnumMap<>(Kind.class);

    for (final Kind kind : Kind.values()) {
      metrics.put(kind, 0L);
    }
  }

  public long getTotal() {
    return total;
  }

  public String getTotalAsString() {
    return timeToString(total);
  }

  private void incTotal(final long value) {
    total += value;
  }

  public long getMetric(final Kind kind) {
    InvariantChecks.checkNotNull(kind);
    return metrics.get(kind);
  }

  public String getMetricAsString(final Kind kind) {
    return timeToString(getMetric(kind));
  }

  public void incMetric(final Kind kind, final long value) {
    InvariantChecks.checkNotNull(kind);
    incTotal(value);
    final long metric = metrics.get(kind);
    metrics.put(kind, metric + value);
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

  @Override
  public String toString() {
    return String.format("TimeMetrics [total=%d, metrics=%s]", total, metrics);
  }
}
