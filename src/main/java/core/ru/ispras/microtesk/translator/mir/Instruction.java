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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Instruction {
  void accept(InsnVisitor visitor);

  static final class Assignment implements Instruction {
    public Local lhs;
    public BinOpcode opc;
    public Operand op1;
    public Operand op2;

    public Assignment(final Local lhs, final Rvalue rhs) {
      this.lhs = lhs;
      this.opc = rhs.opc;
      this.op1 = rhs.op1;
      this.op2 = rhs.op2;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class Extract implements Instruction {
    public Local lhs;
    public Operand rhs;
    public Operand lo;
    public Operand hi;

    public Extract(final Local lhs, final Operand rhs, final Operand lo, final Operand hi) {
      this.lhs = lhs;
      this.rhs = rhs;
      this.lo = lo;
      this.hi = hi;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class Concat implements Instruction {
    public Local lhs;
    public List<Operand> rhs;

    public Concat(final Local lhs, final List<Operand> rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Call implements Instruction {
    public Operand callee;
    public String method;
    public List<Operand> args;
    public Local ret;

    public Call(final Operand callee, final String method, final List<Operand> args, final Local ret) {
      this.callee = callee;
      this.method = method;
      this.args = args;
      this.ret = ret;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Load implements Instruction {
    public Lvalue source;
    public Local target;

    public Load(final Lvalue source, final Local target) {
      this.source = source;
      this.target = target;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Store implements Instruction {
    public Lvalue target;
    public Operand source;

    public Store(final Lvalue target, final Operand source) {
      this.target = target;
      this.source = source;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Disclose implements Instruction {
    public Local target;
    public Operand source;
    public List<Constant> indices;

    public Disclose(final Local target, final Operand source, final List<Constant> indices) {
      this.target = target;
      this.source = source;
      this.indices = indices;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static abstract class Terminator implements Instruction {
    public final List<BasicBlock> successors;

    protected Terminator() {
      this.successors = Collections.emptyList();
    }

    protected Terminator(final List<BasicBlock> successors) {
      this.successors = successors;
    }

    public abstract void accept(InsnVisitor visitor);
  }

  static class Invoke extends Terminator {
    public final Call call;

    public Invoke(final Call call) {
      this.call = call;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Branch extends Terminator {
    public Operand guard;
    public Map<Integer, BasicBlock> target;
    public BasicBlock other;

    public Branch(final BasicBlock next) {
      super(Collections.singletonList(next));
      this.guard = Constant.bitOf(1);
      this.target = Collections.emptyMap();
      this.other = next;
    }

    public Branch(final Operand guard, final BasicBlock bbTaken, final BasicBlock bbOther) {
      super(Arrays.asList(bbTaken, bbOther));
      this.guard = guard;
      this.target = Collections.singletonMap(1, bbTaken);
      this.other = bbOther;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Return extends Terminator {
    public Operand value;

    public Return(final Operand value) {
      this.value = value;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static final class Exception extends Terminator {
    final String message;

    public Exception(final String message) {
      this.message = message;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class Sext implements Instruction {
    public Local lhs;
    public Operand rhs;

    Sext(final Local lhs, final Operand rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class Zext implements Instruction {
    public Local lhs;
    public Operand rhs;

    Zext(final Local lhs, final Operand rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class Conditional implements Instruction {
    public Local lhs;
    public Operand guard;
    public Operand taken;
    public Operand other;

    Conditional(Local lhs, Operand guard, Operand taken, Operand other) {
      this.lhs = lhs;
      this.guard = guard;
      this.taken = taken;
      this.other = other;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }
}

class Local implements Operand {
  public final int id;
  private final MirTy type;

  public Local(final int id, final MirTy type) {
    this.id =  id;
    this.type = type;
  }

  @Override
  public MirTy getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format("%%%d", id);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Local) {
      final Local that = (Local) o;
      return this.id == that.id;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id;
  }
}

class Field extends Lvalue {
  public final Lvalue base;
  public final String name;

  public Field(final Lvalue base, final String name) {
    this.base = base;
    this.name = name;
  }

  @Override
  public MirTy getType() {
    final MirStruct type = (MirStruct) base.getType();
    final TyRef tref = type.fields.get(name);

    return tref.type;
  }

  @Override
  public MirTy getContainerType() {
    return base.getContainerType();
  }

  @Override
  public String toString() {
    return String.format("%s.%s", base.toString(), name);
  }
}

class Index extends Lvalue {
  public final Lvalue base;
  public final Operand index;

  public Index(final Lvalue base, final Operand index) {
    this.base = base;
    this.index = index;
  }

  @Override
  public MirTy getType() {
    final MirArray type = (MirArray) base.getType();
    final TyRef tref = type.ref;

    return tref.type;
  }

  @Override
  public MirTy getContainerType() {
    return base.getContainerType();
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", base.toString(), index.toString());
  }
}

class Rvalue {
  public final BinOpcode opc;
  public final Operand op1;
  public final Operand op2;

  Rvalue(final BinOpcode opc, final Operand op1, final Operand op2) {
    this.opc = opc;
    this.op1 = op1;
    this.op2 = op2;
  }

  public MirTy getType() {
    return opc.typeOf(op1, op2);
  }
}

class Closure implements Operand {
  public final String callee;
  public final FuncTy type;
  public final List<Operand> upvalues;

  public Closure(final String callee, final List<Operand> upvalues) {
    final List<MirTy> types = new java.util.ArrayList<>();
    for (final Operand upval : upvalues) {
      types.add(upval.getType());
    }
    this.callee = callee;
    this.type = new FuncTy(VoidTy.VALUE, types);
    this.upvalues = upvalues;
  }

  @Override
  public MirTy getType() {
    return type;
  }
}

interface BinOpcode {
  Rvalue make(Operand lhs, Operand rhs);
  MirTy typeOf(Operand lhs, Operand rhs);
}

interface ConstEvaluated {
  Constant evalConst(Constant lhs, Constant rhs);
}

enum UnOpcode implements BinOpcode {
  Use;

  public Rvalue make(final Operand opnd) {
    return new Rvalue(this, opnd, null);
  }

  public Rvalue make(final Operand lhs, final Operand rhs) {
    if (lhs != null) {
      return new Rvalue(this, lhs, rhs);
    }
    return new Rvalue(this, rhs, lhs);
  }

  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    if (lhs != null) {
      return lhs.getType();
    }
    return rhs.getType();
  }
}

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
