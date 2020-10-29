/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;

enum CmpOpcode implements BinOpcode, ConstEvaluated {
  /// The `==` operator (equality)
  Eq {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return lhs.compareTo(rhs) == 0;
    }
  },
  /// The `!=` operator (not equal to)
  Ne {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return lhs.compareTo(rhs) != 0;
    }
  },
  /// The `<` operator (less than)
  Ult {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.ult(lhs, rhs);
    }
  },
  /// The `<=` operator (less than or equal to)
  Ule {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.ule(lhs, rhs);
    }
  },
  /// The `>=` operator (greater than or equal to)
  Uge {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.uge(lhs, rhs);
    }
  },
  /// The `>` operator (greater than)
  Ugt {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.ugt(lhs, rhs);
    }
  },
  Slt {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.slt(lhs, rhs);
    }
  },
  Sle {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.sle(lhs, rhs);
    }
  },
  Sge {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.sge(lhs, rhs);
    }
  },
  Sgt {
    @Override
    boolean compare(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.sgt(lhs, rhs);
    }
  };

  @Override
  public Rvalue make(final Operand lhs, final Operand rhs) {
    return new Rvalue(this, lhs, rhs);
  }

  @Override
  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    return new IntTy(1);
  }

  @Override
  public Constant evalConst(final Constant lhs, final Constant rhs) {
    return constantOf(BitVector.valueOf(compare(bitVectorOf(lhs), bitVectorOf(rhs))));
  }

  public static Constant constantOf(BitVector value) {
    return Constant.valueOf(value.getBitSize(), value.bigIntegerValue());
  }

  public static BitVector bitVectorOf(Constant value) {
    return BitVector.valueOf(value.getValue(), value.getType().getSize());
  }

  abstract boolean compare(BitVector lhs, BitVector rhs);
}
