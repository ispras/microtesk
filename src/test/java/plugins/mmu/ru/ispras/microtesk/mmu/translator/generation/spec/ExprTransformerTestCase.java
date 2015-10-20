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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import static org.junit.Assert.*;

import org.junit.Test;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

public class ExprTransformerTestCase {

  @Test
  public void testLeftShift() {
    testLeftShift(StandardOperation.BVASHL, 32, 0);
    testLeftShift(StandardOperation.BVLSHL, 32, 0);

    testLeftShift(StandardOperation.BVASHL, 32, 1);
    testLeftShift(StandardOperation.BVLSHL, 32, 1);

    testLeftShift(StandardOperation.BVASHL, 32, 16);
    testLeftShift(StandardOperation.BVLSHL, 32, 16);

    testLeftShift(StandardOperation.BVASHL, 64, 16);
    testLeftShift(StandardOperation.BVLSHL, 64, 16);

    testLeftShift(StandardOperation.BVASHL, 32, 32);
    testLeftShift(StandardOperation.BVLSHL, 32, 32);
  }

  @Test
  public void testRightShift() {
    testRightShift(StandardOperation.BVLSHR, 32, 0);
    testRightShift(StandardOperation.BVLSHR, 32, 1);
    testRightShift(StandardOperation.BVLSHR, 32, 16);
    testRightShift(StandardOperation.BVLSHR, 64, 16);
    testRightShift(StandardOperation.BVLSHR, 32, 32);
  }

  private static void testLeftShift(
      final StandardOperation operator,
      final int bitSize,
      final int shiftAmount) {

    final Node x =
        new NodeVariable("x", DataType.BIT_VECTOR(bitSize));

    final Node initial = new NodeOperation(
        operator,
        x,
        NodeValue.newInteger(shiftAmount)
        );

    final Node expected = shiftAmount % bitSize == 0 ?
        x :
        new NodeOperation(
        StandardOperation.BVCONCAT,
        newField(x, 0, bitSize - shiftAmount - 1),
        NodeValue.newBitVector(BitVector.newEmpty(shiftAmount))
        );

    final Node result = transform(initial);

    /*
    System.out.println("Initial:  " + initial);
    System.out.println("Result:   " + result);
    System.out.println("Expected: " + expected);
    */

    assertEquals(expected, result);
  }

  private static void testRightShift(
      final StandardOperation operator,
      final int bitSize,
      final int shiftAmount) {

    final Node x =
        new NodeVariable("x", DataType.BIT_VECTOR(bitSize));

    final Node initial = new NodeOperation(
        operator,
        x,
        NodeValue.newInteger(shiftAmount)
        );

    final Node expected = shiftAmount % bitSize == 0 ?
        x :
        new NodeOperation(
        StandardOperation.BVCONCAT,
        NodeValue.newBitVector(BitVector.newEmpty(shiftAmount)),
        newField(x, shiftAmount, bitSize - 1)
        );

    final Node result = transform(initial);

    /*
    System.out.println("Initial:  " + initial);
    System.out.println("Result:   " + result);
    System.out.println("Expected: " + expected);
    */

    assertEquals(expected, result);
  }

  @Test
  public void testAndMask() {
    final int bitSize = 32;

    final Node x =
        new NodeVariable("x", DataType.BIT_VECTOR(bitSize));

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0xFFFFFFFF, 32),
        x
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.newEmpty(32),
        NodeValue.newBitVector(BitVector.newEmpty(32))
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0x0000FFFF, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.newEmpty(16)),
            newField(x, 0, 15)
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0xFFFF0000, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 16, 31),
            NodeValue.newBitVector(BitVector.newEmpty(16))
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0xFF00FF00, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 24, 31),
            NodeValue.newBitVector(BitVector.newEmpty(8)),
            newField(x, 8, 15),
            NodeValue.newBitVector(BitVector.newEmpty(8))
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0x00FF00FF, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.newEmpty(8)),
            newField(x, 16, 23),
            NodeValue.newBitVector(BitVector.newEmpty(8)),
            newField(x, 0, 7)
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0x00000001, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.newEmpty(31)),
            newField(x, 0, 0)
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0x80000000, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 31, 31),
            NodeValue.newBitVector(BitVector.newEmpty(31))
            )
        );

    testBitMask(
        StandardOperation.BVAND,
        BitVector.valueOf(0x80000001, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 31, 31),
            NodeValue.newBitVector(BitVector.newEmpty(30)),
            newField(x, 0, 0)
            )
        );
  }

  @Test
  public void testOrMask() {
    final int bitSize = 32;

    final Node x =
        new NodeVariable("x", DataType.BIT_VECTOR(bitSize));

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0xFFFFFFFF, 32),
        NodeValue.newBitVector(BitVector.valueOf(-1, 32))
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.newEmpty(32),
        x
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0x0000FFFF, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 16, 31),
            NodeValue.newBitVector(BitVector.valueOf(-1, 16))
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0xFFFF0000, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.valueOf(-1, 16)),
            newField(x, 0, 15)
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0xFF00FF00, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.valueOf(-1, 8)),
            newField(x, 16, 23),
            NodeValue.newBitVector(BitVector.valueOf(-1, 8)),
            newField(x, 0, 7)
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0x00FF00FF, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 24, 31),
            NodeValue.newBitVector(BitVector.valueOf(-1, 8)),
            newField(x, 8, 15),
            NodeValue.newBitVector(BitVector.valueOf(-1, 8))
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0x00000001, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            newField(x, 1, 31),
            NodeValue.newBitVector(BitVector.valueOf(-1, 1))
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0x80000000, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.valueOf(-1, 1)),
            newField(x, 0, 30)
            )
        );

    testBitMask(
        StandardOperation.BVOR,
        BitVector.valueOf(0x80000001, 32),
        new NodeOperation(
            StandardOperation.BVCONCAT,
            NodeValue.newBitVector(BitVector.valueOf(-1, 1)),
            newField(x, 1, 30),
            NodeValue.newBitVector(BitVector.valueOf(-1, 1))
            )
        );
  }

  private static void testBitMask(
      final StandardOperation operator,
      final BitVector mask,
      final Node expected) {

    final Node x =
        new NodeVariable("x", DataType.BIT_VECTOR(mask.getBitSize()));

    final Node initial = new NodeOperation(
        operator,
        x,
        NodeValue.newBitVector(mask)
        );

    final Node result = transform(initial);

    System.out.println("Initial:  " + initial);
    System.out.println("Result:   " + result);
    System.out.println("Expected: " + expected);

    assertEquals(expected, result);
  }

  private static Node transform(final Node expr) {
    final ExprTransformer transformer = new ExprTransformer();
    return transformer.transform(expr);
  }

  private static Node newField(final Node expr, final int from, final int to) {
    if (expr.getDataType().getSize() == to - from + 1) {
      return expr;
    }

    return new NodeOperation(
        StandardOperation.BVEXTRACT,
        NodeValue.newInteger(to),
        NodeValue.newInteger(from),
        expr
        );
  }
}
