/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

import java.util.Arrays;
import java.util.Collection;

public final class CoverageExplorer {
  private CoverageExplorer() {}

  public static Statistics collectStatistics(
      final MmuSubsystem mmu,
      final MemoryAccessType access) {
    final MemoryCoverageExtractor extractor = new MemoryCoverageExtractor(mmu);
    return collectStatistics(extractor.getPaths(access));
  }

  public static Statistics collectStatistics(final Collection<MemoryAccessPath> paths) {
    if (paths.isEmpty()) {
      return new Statistics();
    }
    final int[] lengths = new int[paths.size()];

    int i = 0;
    for (final MemoryAccessPath path : paths) {
      lengths[i++] = path.getTransitions().size();
    }
    return new Statistics(lengths);
  }

  public final static class Statistics {
    public final int min;
    public final int max;
    public final double median;
    public final double arithmeticMean;
    public final double expectedValue;

    public Statistics() {
      this.min = 0;
      this.max = 0;
      this.arithmeticMean = 0;
      this.expectedValue = 0;
      this.median = 0;
    }

    public Statistics(final int[] samples) {
      Arrays.sort(samples);

      this.min = samples[0];
      this.max = samples[samples.length - 1];
      this.median = median(samples);
      this.arithmeticMean = sum(samples) / (double) samples.length;
      this.expectedValue = expected(samples);
    }

    public static long sum(final int[] samples) {
      long s = 0;
      for (int n : samples) {
        s += n;
      }
      return s;
    }

    public static double expected(final int[] samples) {
      double value = 0;

      int i = 0;
      final int len = samples.length;
      while (i < len) {
        final int sample = samples[i++];

        int count = 1;
        while (i < len && samples[i] == sample) {
          ++count;
          ++i;
        }
        value += sample * count / (double) len;
      }
      return value;
    }
    
    public static double median(final int[] samples) {
      final int left = (samples.length - 1) / 2;
      final int right = samples.length / 2;

      return (samples[left] + samples[right]) / 2.0;
    }

    @Override
    public String toString() {
      return String.format(
          "Min: %d, Max: %d, Avg: %f, Median: %d, EV: %f",
          min, max, arithmeticMean, (int) median, expectedValue);
    }
  }
}
