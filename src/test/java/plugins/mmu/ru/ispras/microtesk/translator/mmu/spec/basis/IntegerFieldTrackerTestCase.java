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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class IntegerFieldTrackerTestCase {
  @Test
  public void test() {
    final IntegerVariable var = new IntegerVariable("VA", 32); 
    final IntegerFieldTracker tracker = new IntegerFieldTracker(var);

    assertEquals(Collections.singletonList(
        new IntegerField(var, 0, 31)), tracker.getFields());

    tracker.exclude(8, 15);
    assertEquals(Arrays.asList(
        new IntegerField(var, 0, 7), new IntegerField(var, 16, 31)), tracker.getFields());

    tracker.exclude(12, 23);
    assertEquals(Arrays.asList(
        new IntegerField(var, 0, 7), new IntegerField(var, 24, 31)), tracker.getFields());

    tracker.exclude(31, 31);
    assertEquals(Arrays.asList(
        new IntegerField(var, 0, 7), new IntegerField(var, 24, 30)), tracker.getFields());

    tracker.exclude(0, 0);
    assertEquals(Arrays.asList(
        new IntegerField(var, 1, 7), new IntegerField(var, 24, 30)), tracker.getFields());

    tracker.excludeAll();
    assertEquals(Collections.emptyList(), tracker.getFields());
  }
}
