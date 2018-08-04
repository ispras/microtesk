package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Instruction {}

final class Assignment implements Instruction {
  private final Lvalue lhs;
  private final Opcode opc;
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
  public final Opcode opc;
  public final Operand op1;
  public final Operand op2;

  Rvalue(final Opcode opc, final Operand op1, final Operand op2) {
    this.opc = opc;
    this.op1 = op1;
    this.op2 = op2;
  }
}

class Cast extends Rvalue {
  public final DataType type;

  public Cast(final Rvalue rhs, final DataType type) {
    super(rhs.opc, rhs.op1, rhs.op2);
    this.type = type;
  }
}

class Constant implements Operand {
  private final Data value;

  Constant(final Data value) {
    this.value = value;
  }
}

class Closure implements Operand {
  // TODO
  // signature
  // environment
  // Mir
}

enum Opcode {
  /// The `+` operator (addition)
  Add,
  /// The `-` operator (subtraction)
  Sub,
  /// The `*` operator (multiplication)
  Mul,
  /// The `/` operator (division)
  Div,
  /// The `%` operator (modulus)
  Rem,
  /// The `^` operator (bitwise xor)
  BitXor,
  /// The `&` operator (bitwise and)
  BitAnd,
  /// The `|` operator (bitwise or)
  BitOr,
  /// The `<<` operator (shift left)
  Shl,
  /// The `>>` operator (shift right)
  Shr,
  /// The `==` operator (equality)
  Eq,
  /// The `<` operator (less than)
  Lt,
  /// The `<=` operator (less than or equal to)
  Le,
  /// The `!=` operator (not equal to)
  Ne,
  /// The `>=` operator (greater than or equal to)
  Ge,
  /// The `>` operator (greater than)
  Gt,
  /// The `!` operator for logical inversion
  Not,
  /// Pass operand value as is
  Use;

  public Rvalue make(final Operand op) {
    return make(op, null);
  }

  public Rvalue make(final Operand op1, final Operand op2) {
    return new Rvalue(this, op1, op2);
  }
}
