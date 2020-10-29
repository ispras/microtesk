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

enum BvOpcode implements BinOpcode, ConstEvaluated {
  /// The `+` operator (addition)
  Add {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.add(lhs, rhs);
    }
  },
  /// The `-` operator (subtraction)
  Sub {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.sub(lhs, rhs);
    }
  },
  /// The `*` operator (multiplication)
  Mul {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.mul(lhs, rhs);
    }
  },

  /// The `/` operator (division)
  Udiv {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.udiv(lhs, rhs);
    }
  },
  /// The `%` operator (modulus)
  Urem {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.urem(lhs, rhs);
    }
  },
  /// The `/` operator (division)
  Sdiv {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.sdiv(lhs, rhs);
    }
  },
  /// The `%` operator (modulus)
  Srem {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.srem(lhs, rhs);
    }
  },
  /// The `^` operator (bitwise xor)
  Xor {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.xor(lhs, rhs);
    }
  },
  /// The `&` operator (bitwise and)
  And {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.and(lhs, rhs);
    }
  },
  /// The `|` operator (bitwise or)
  Or {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.or(lhs, rhs);
    }
  },
  /// The `<<` operator (shift left)
  Shl {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.shl(lhs, rhs);
    }
  },
  /// The `>>` operator (shift right)
  Ashr {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.ashr(lhs, rhs);
    }
  },
  Lshr {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.lshr(lhs, rhs);
    }
  },
  Rotr {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.rotr(lhs, rhs);
    }
  },
  Rotl {
    @Override
    BitVector evalBitVector(final BitVector lhs, final BitVector rhs) {
      return BitVectorMath.rotl(lhs, rhs);
    }
  };

  @Override
  public Rvalue make(final Operand op1, final Operand op2) {
    return new Rvalue(this, op1, op2);
  }

  @Override
  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    return lhs.getType();
  }

  @Override
  public Constant evalConst(final Constant lhs, final Constant rhs) {
    return constantOf(evalBitVector(bitVectorOf(lhs), bitVectorOf(rhs)));
  }

  public static Constant constantOf(BitVector value) {
    return Constant.valueOf(value.getBitSize(), value.bigIntegerValue());
  }

  public static BitVector bitVectorOf(Constant value) {
    return BitVector.valueOf(value.getValue(), value.getType().getSize());
  }

  abstract BitVector evalBitVector(BitVector lhs, BitVector rhs);
}
