/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Nodes;

import java.util.Arrays;
import java.util.Collections;

public class IntegerFieldTrackerTestCase {
  @Test
  public void test() {
    final Variable var = new Variable("VA", DataType.bitVector(32)); 
    final IntegerFieldTracker tracker = new IntegerFieldTracker(var);

    Assert.assertEquals(
        Collections.singletonList(
            Nodes.bvextract(31, 0, var)),
        tracker.getFields());

    tracker.exclude(8, 15);
    Assert.assertEquals(
        Arrays.asList(
            Nodes.bvextract(7, 0, var),
            Nodes.bvextract(31, 16, var)),
        tracker.getFields());

    tracker.exclude(12, 23);
    Assert.assertEquals(
        Arrays.asList(
            Nodes.bvextract(7, 0, var),
            Nodes.bvextract(31, 24, var)),
        tracker.getFields());

    tracker.exclude(31, 31);
    Assert.assertEquals(
        Arrays.asList(
            Nodes.bvextract(7, 0, var),
            Nodes.bvextract(30, 24, var)),
        tracker.getFields());

    tracker.exclude(0, 0);
    Assert.assertEquals(
        Arrays.asList(
            Nodes.bvextract(7, 1, var),
            Nodes.bvextract(30, 24, var)),
        tracker.getFields());

    tracker.excludeAll();
    Assert.assertEquals(Collections.emptyList(), tracker.getFields());
  }

  @Test
  public void test2() {
    final Variable var = new Variable("PA", DataType.bitVector(36)); 
    final IntegerFieldTracker tracker = new IntegerFieldTracker(var);

    tracker.exclude(5, 11);
    tracker.exclude(12, 35);

    Assert.assertEquals(
        Collections.singletonList(
            Nodes.bvextract(4, 0, var)),
        tracker.getFields());
  }

  @Test
  public void test3() {
    final Variable var = new Variable("PA", DataType.bitVector(36)); 
    final IntegerFieldTracker tracker = new IntegerFieldTracker(var);
  
    tracker.exclude(11, 5);
    tracker.exclude(35, 12);

    Assert.assertEquals(
        Collections.singletonList(
            Nodes.bvextract(4, 0, var)),
        tracker.getFields());
  }
}
