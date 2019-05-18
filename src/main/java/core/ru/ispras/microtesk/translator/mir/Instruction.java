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
}

final class Assignment implements Instruction {
  public final Lvalue lhs;
  public final BinOpcode opc;
  public final Operand op1;
  public final Operand op2;

  public Assignment(final Lvalue lhs, final Rvalue rhs) {
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

class Extract implements Instruction {
  public final Lvalue lhs;
  public final Operand rhs;
  public final Operand lo;
  public final Operand hi;

  public Extract(final Lvalue lhs, final Operand rhs, final Operand lo, final Operand hi) {
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

class Concat implements Instruction {
  public final Lvalue lhs;
  public final List<Operand> rhs;

  public Concat(final Lvalue lhs, final List<Operand> rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

final class Call implements Instruction {
  public final Operand callee;
  public final String method;
  public final List<Operand> args;
  public final Local ret;

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

final class Load implements Instruction {
  public final Lvalue source;
  public final Local target;

  public Load(final Lvalue source, final Local target) {
    this.source = source;
    this.target = target;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

final class Store implements Instruction {
  public final Lvalue target;
  public final Operand source;

  public Store(final Lvalue target, final Operand source) {
    this.target = target;
    this.source = source;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

final class Disclose implements Instruction {
  public final Local target;
  public final Operand source;
  public final List<Constant> indices;

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

abstract class Terminator implements Instruction {
  public final List<BasicBlock> successors;

  protected Terminator() {
    this.successors = Collections.emptyList();
  }

  protected Terminator(final List<BasicBlock> successors) {
    this.successors = successors;
  }

  public abstract void accept(InsnVisitor visitor);
}

class Invoke extends Terminator {
  public final Call call;

  public Invoke(final Call call) {
    this.call = call;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

final class Branch extends Terminator {
  public final Operand guard;
  public final Map<Integer, BasicBlock> target;
  public final BasicBlock other;

  public Branch(final BasicBlock next) {
    super(Collections.singletonList(next));
    this.guard = new Constant(1, BigInteger.ONE);
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

final class Return extends Terminator {
  public final Operand value;

  public Return(final Operand value) {
    this.value = value;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

final class Exception extends Terminator {
  final String message;

  public Exception(final String message) {
    this.message = message;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

interface Operand {
  MirTy getType();
}

abstract class Lvalue implements Operand {
  @Override
  abstract public MirTy getType();
  abstract public MirTy getContainerType();
}

class Local extends Lvalue {
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
  public MirTy getContainerType() {
    return getType();
  }

  @Override
  public String toString() {
    return String.format("%%%d", id);
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

class Static extends Lvalue {
  public final String name;
  public final int version;
  private final MirTy type;

  public Static(final String name, final MirTy type) {
    this(name, 0, type);
  }

  public Static(String name, int version, MirTy type) {
    this.name = name;
    this.version = version;
    this.type = type;
  }

  @Override
  public MirTy getType() {
    return type;
  }

  @Override
  public MirTy getContainerType() {
    return getType();
  }

  @Override
  public String toString() {
    return name;
  }

  public Static newVersion(final int n) {
    return new Static(name, n, type);
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

class Sext implements Instruction {
  public final Lvalue lhs;
  public final Operand rhs;

  Sext(final Lvalue lhs, final Operand rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

class Zext implements Instruction {
  public final Lvalue lhs;
  public final Operand rhs;

  Zext(final Lvalue lhs, final Operand rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public void accept(final InsnVisitor visitor) {
    visitor.visit(this);
  }
}

class Constant implements Operand {
  private final int bits;
  private final long cache;
  private final BigInteger value;

  public Constant(final int bits, final long value) {
    this.bits = bits;
    this.cache = value;
    this.value = BigInteger.valueOf(value);
  }

  public Constant(final int bits, final BigInteger value) {
    this.bits = bits;
    this.cache = -1;
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public MirTy getType() {
    return new IntTy(bits);
  }

  @Override
  public String toString() {
    return value.toString();
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
    return toConstant(evalBitVector(toBitVector(lhs), toBitVector(rhs)));
  }

  abstract BitVector evalBitVector(BitVector lhs, BitVector rhs);

  public static BitVector toBitVector(final Constant value) {
    return BitVector.valueOf(value.getValue(), value.getType().getSize());
  }

  public static Constant toConstant(final BitVector value) {
    return new Constant(value.getBitSize(), value.bigIntegerValue());
  }
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
    return toConstant(BitVector.valueOf(compare(toBitVector(lhs), toBitVector(rhs))));
  }

  abstract boolean compare(BitVector lhs, BitVector rhs);

  public static BitVector toBitVector(final Constant value) {
    return BitVector.valueOf(value.getValue(), value.getType().getSize());
  }

  public static Constant toConstant(final BitVector value) {
    return new Constant(value.getBitSize(), value.bigIntegerValue());
  }
}
