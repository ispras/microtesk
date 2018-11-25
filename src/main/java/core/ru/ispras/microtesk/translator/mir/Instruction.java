package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeValue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Instruction {}

final class Assignment implements Instruction {
  private final Lvalue lhs;
  private final BinOpcode opc;
  private final Operand op1;
  private final Operand op2;

  public Assignment(final Lvalue lhs, final Rvalue rhs) {
    this.lhs = lhs;
    this.opc = rhs.opc;
    this.op1 = rhs.op1;
    this.op2 = rhs.op2;
  }
}

class Extract implements Instruction {
  private final Lvalue lhs;
  private final Operand rhs;
  private final Operand lo;
  private final Operand hi;

  public Extract(final Lvalue lhs, final Operand rhs, final Operand lo, final Operand hi) {
    this.lhs = lhs;
    this.rhs = rhs;
    this.lo = lo;
    this.hi = hi;
  }
}

class Concat implements Instruction {
  private final Lvalue lhs;
  private final List<Operand> rhs;

  public Concat(final Lvalue lhs, final List<Operand> rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
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
}

final class Load implements Instruction {
  public final Lvalue source;
  public final Local target;

  public Load(final Lvalue source, final Local target) {
    this.source = source;
    this.target = target;
  }
}

final class Store implements Instruction {
  public final Lvalue target;
  public final Operand source;

  public Store(final Lvalue target, final Operand source) {
    this.target = target;
    this.source = source;
  }
}

class Terminator implements Instruction {
  public final List<BasicBlock> successors;

  protected Terminator() {
    this.successors = Collections.emptyList();
  }

  protected Terminator(final BasicBlock bb) {
    this.successors = Collections.singletonList(bb);
  }

  protected Terminator(final List<BasicBlock> successors) {
    this.successors = successors;
  }
}

class Invoke extends Terminator {
  public final Call call;

  public Invoke(final Call call) {
    this.call = call;
  }
}

final class Branch extends Terminator {
  private final Map<Integer, BasicBlock> target;
  private final BasicBlock other;

  public Branch(final Operand cond, final BasicBlock bbTaken, final BasicBlock bbOther) {
    super(Arrays.asList(bbTaken, bbOther));
    this.target = Collections.singletonMap(1, bbTaken);
    this.other = bbOther;
  }

  public BasicBlock getPathTaken() {
    return successors.get(successors.size() - 1);
  }
}

final class Return extends Terminator {
  public final Lvalue value;

  public Return(final Lvalue value) {
    this.value = value;
  }
}

final class Exception extends Terminator {
  final String message;

  public Exception(final String message) {
    this.message = message;
  }
}

interface Operand {}

class Lvalue implements Operand {}

class Local extends Lvalue {
  private final int id;

  public Local(final int id) {
    this.id =  id;
  }
}

class Field extends Lvalue {
  private final Lvalue base;
  private final String name;
  private final DataType type;

  public Field(final Lvalue base, final String name, final DataType type) {
    this.base = base;
    this.name = name;
    this.type = type;
  }
}

class Index extends Lvalue {
  private final Lvalue base;
  private final Local index;

  public Index(final Lvalue base, final Local index) {
    this.base = base;
    this.index = index;
  }
}

class Static extends Lvalue {
  private final String name;

  public Static(final String name) {
    this.name = name;
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
}

class Sext extends Rvalue {
  public final int bits;

  Sext(final int bits, final Rvalue rv) {
    super(rv.opc, rv.op1, rv.op2);
    this.bits = bits;
  }
}

class Zext extends Rvalue {
  public final int bits;

  Zext(final int bits, final Rvalue rv) {
    super(rv.opc, rv.op1, rv.op2);
    this.bits = bits;
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
}

class Closure implements Operand {
  // TODO
  // signature
  // environment
  // Mir
}

interface BinOpcode {
  Rvalue make(Operand lhs, Operand rhs);
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
}
