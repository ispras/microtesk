package ru.ispras.microtesk.translator.mir;

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
  public final List<Operand> args;
  public final Local ret;

  public Call(final Operand callee, final List<Operand> args, final Local ret) {
    this.callee = callee;
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
  private final int id;
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
  private final Lvalue base;
  private final String name;

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
  private final Lvalue base;
  private final Local index;

  public Index(final Lvalue base, final Local index) {
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
  private final String name;
  private final MirTy type;

  public Static(final String name, final MirTy type) {
    this.name = name;
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
  // TODO
  // signature
  // environment
  // Mir

  @Override
  public MirTy getType() {
    return new IntTy(64);
  }
}

interface BinOpcode {
  Rvalue make(Operand lhs, Operand rhs);
  MirTy typeOf(Operand lhs, Operand rhs);
}

enum BvOpcode implements BinOpcode {
  /// The `+` operator (addition)
  Add,
  /// The `-` operator (subtraction)
  Sub,
  /// The `*` operator (multiplication)
  Mul,
  /// The `/` operator (division)
  Udiv,
  /// The `%` operator (modulus)
  Urem,
  /// The `/` operator (division)
  Sdiv,
  /// The `%` operator (modulus)
  Srem,
  /// The `^` operator (bitwise xor)
  Xor,
  /// The `&` operator (bitwise and)
  And,
  /// The `|` operator (bitwise or)
  Or,
  /// The `<<` operator (shift left)
  Shl,
  /// The `>>` operator (shift right)
  Ashr,
  Lshr;

  @Override
  public Rvalue make(final Operand op1, final Operand op2) {
    return new Rvalue(this, op1, op2);
  }

  @Override
  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    return lhs.getType();
  }
}

enum CmpOpcode implements BinOpcode {
  /// The `==` operator (equality)
  Eq,
  /// The `!=` operator (not equal to)
  Ne,
  /// The `<` operator (less than)
  Ult,
  /// The `<=` operator (less than or equal to)
  Ule,
  /// The `>=` operator (greater than or equal to)
  Uge,
  /// The `>` operator (greater than)
  Ugt,
  Slt,
  Sle,
  Sge,
  Sgt;

  @Override
  public Rvalue make(final Operand lhs, final Operand rhs) {
    return new Rvalue(this, lhs, rhs);
  }

  @Override
  public MirTy typeOf(final Operand lhs, final Operand rhs) {
    return new IntTy(1);
  }
}
