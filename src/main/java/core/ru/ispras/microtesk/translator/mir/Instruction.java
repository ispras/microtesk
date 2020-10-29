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
