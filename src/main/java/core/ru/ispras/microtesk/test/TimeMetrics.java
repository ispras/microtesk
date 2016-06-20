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

public final class TimeMetrics {
  /*
  private long totalTime;
  private long launchTime;
  private long parsingTime;
  private long sequencingTime;
  private long generationTime;
  private long simulationTime;
  private long printingTime;
  */

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
