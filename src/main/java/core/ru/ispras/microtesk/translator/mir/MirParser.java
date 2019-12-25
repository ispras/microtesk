package ru.ispras.microtesk.translator.mir;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static ru.ispras.microtesk.translator.mir.Instruction.*;

public class MirParser {
  private final BufferedReader reader;
  private final List<String> lines = new java.util.ArrayList<>();

  public MirParser(final InputStream is) throws IOException {
    this(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
  }

  public MirParser(final Reader input) throws IOException {
    this.reader = new BufferedReader(input);
    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
      lines.add(line);
    }
  }

  public MirContext parse() {
    final MirContext ctx = Parser2.nextContext(newScanner(lines.get(0)));

    final Map<String, MirBlock> blocks = new java.util.HashMap<>();
    for (final String line : lines.subList(1, lines.size())) {
      if (line.matches("\\w+:")) {
        final String name = line.substring(0, line.indexOf(":"));
        blocks.put(name, ctx.newBlock());
      }
    }
    MirBlock block = null;

    for (final String line : lines.subList(1, lines.size())) {
      if (line.matches("\\w+:")) {
        final String name = line.substring(0, line.indexOf(":"));
        block = blocks.get(name);
      } else {
        final Parser2 parser = new Parser2(newScanner(line), blocks, ctx);
        block.append(parser.nextInsn());
      }
    }
    return ctx;
  }

  private static Scanner newScanner(final String line) {
    return new Scanner(preprocess(line));
  }

  private static String preprocess(final String line) {
    return line.replaceAll("[,()<>]", "").replaceAll("([\\[\\]\\{\\}])", " $1 ").trim();
  }

  private enum TokenKind {
    LBRACE("\\{"),
    RBRACE("\\}"),
    LBRACKET("\\["),
    RBRACKET("\\]"),
    LANGLE("\\<"),
    RANGLE("\\>"),
    LPAREN("\\("),
    RPAREN("\\)"),
    EQUALS("="),
    DOT("\\."),
    LOCAL("\\%\\w+") {
      @Override
      public String next(final Scanner s) {
        return s.next(patterns.get(0)).substring(1);
      }
    },
    IDENT("\\w+"),
    IDENT_HIDDEN("\\.?\\w+"),
    EOF("");
    
    public boolean nextIn(final Scanner s) {
      return s.hasNext(patterns.get(0));
    }

    public String next(final Scanner s) {
      return s.next(patterns.get(0));
    }

    protected final List<Pattern> patterns;

    private TokenKind(final String s) {
      this.patterns = Collections.singletonList(Pattern.compile(s));
    }
  }

  private final static class Token {
    public final TokenKind kind;
    public final String value;

    Token(final TokenKind kind, final String value) {
      this.kind = kind;
      this.value = value;
    }
  }

  private static final class Parser2 {
    private final Scanner scanner;
    private final Map<String, MirBlock> blocks;
    private final List<MirTy> locals;

    Parser2(final Scanner s, final Map<String, MirBlock> blocks, final MirContext ctx) {
      this.scanner = s;
      this.blocks = blocks;
      this.locals = ctx.locals;
    }

    public Instruction nextInsn() {
      final Scanner s = this.scanner;
      for (final Instruction insn = nextAssign(s); insn != null; ) {
        return insn;
      }
      for (final Branch insn = nextBranch(blocks, s); insn != null; ) {
        return insn;
      }
      if (s.hasNext("call")) {
        return nextCall("", s);
      }
      for (final Return insn = nextReturn(s); insn != null; ) {
        return insn;
      }
      for (final Store insn = nextStore(s); insn != null; ) {
        return insn;
      }
      throw new IllegalArgumentException();
    }

    public static MirContext nextContext(final Scanner s) {
      final String name = s.next();
      final FuncTy type = nextFunc(s);

      return new MirContext(name, type);
    }

    public static Token nextToken(final TokenKind kind, final Scanner s) {
      if (kind.nextIn(s)) {
        return new Token(kind, kind.next(s));
      }
      return null;
    }

    public static MirTy nextType(final Scanner s) {
      for (final MirArray type = nextArray(s); type != null; ) {
        return type;
      }
      for (final MirStruct type = nextStruct(s); type != null; ) {
        return type;
      }
      for (final FuncTy type = nextFunc(s); type != null; ) {
        return type;
      }
      for (final Token token = nextToken(TokenKind.IDENT, s); token != null; ) {
        return Types.valueOf(token.value);
      }
      throw new IllegalStateException();
    }

    private static MirArray nextArray(final Scanner s) {
      if (TokenKind.LBRACKET.nextIn(s)) {
        nextToken(TokenKind.LBRACKET, s);
        final BigInteger size = s.nextBigInteger();
        final MirTy type = nextType(s);
        nextToken(TokenKind.RBRACKET, s);

        return new MirArray(size, new TyRef(type));
      }
      return null;
    }

    private static MirStruct nextStruct(final Scanner s) {
      if (TokenKind.LBRACE.nextIn(s)) {
        TokenKind.LBRACE.next(s);

        final Map<String, TyRef> fields = new java.util.LinkedHashMap<>();
        while (!TokenKind.RBRACE.nextIn(s)) {
          final MirTy type = nextType(s);
          final String name = TokenKind.IDENT.next(s);

          fields.put(name, new TyRef(type));
        }
        TokenKind.RBRACE.next(s);

        return new MirStruct(fields);
      }
      return null;
    }

    private static FuncTy nextFunc(final Scanner s) {
      if (s.hasNext("func")) {
        TokenKind.IDENT.next(s);
        final MirTy retty = nextType(s);

        TokenKind.LBRACKET.next(s);
        final List<MirTy> params = new java.util.ArrayList<>();
        while (!TokenKind.RBRACKET.nextIn(s)) {
          params.add(nextType(s));
        }
        TokenKind.RBRACKET.next(s);

        return new FuncTy(retty, params);
      }
      return null;
    }

    public Call nextCall(final String target, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy retty = nextType(s);
      final Local ret;
      if (target.isEmpty()) {
        ret = null;
      } else {
        ret = newLocal(target, retty);
      }

      final String method = s.next();

      final Operand callee;
      final List<Operand> args = new java.util.ArrayList<>();

      if (TokenKind.LOCAL.nextIn(s)) {
        final String calleeName = TokenKind.LOCAL.next(s);
        final List<MirTy> params = new java.util.ArrayList<>();
        while (s.hasNext()) {
          final MirTy type = nextType(s);
          final Operand arg = nextOperand(type, s);
          params.add(type);
          args.add(arg);
        }
        callee = newLocal(calleeName, new FuncTy(retty, params));
      } else {
        callee = nextClosure(s);
        while (s.hasNext()) {
          final MirTy type = nextType(s);
          final Operand arg = nextOperand(type, s);
          args.add(arg);
        }
      }

      return new Call(callee, method, args, ret);
    }

    public Operand nextOperand(final MirTy type, final Scanner s) {
      if (TokenKind.LOCAL.nextIn(s)) {
        return newLocal(TokenKind.LOCAL.next(s), type);
      }
      if (TokenKind.LBRACE.nextIn(s)) {
        return nextClosure(s);
      }
      try {
        return nextConst(type, s);
      } catch (final java.util.InputMismatchException e) { // FIXME
        return new Constant(type.getSize(), 0);
      }
    }

    public static Constant nextConst(final MirTy type, final Scanner s) {
      return new Constant(type.getSize(), s.nextBigInteger());
    }

    public Closure nextClosure(final Scanner s) {
      TokenKind.LBRACE.next(s);
      final List<Operand> upvalues = new java.util.ArrayList<>();
      while (!TokenKind.RBRACE.nextIn(s)) {
        final MirTy argty = nextType(s);
        final Operand arg = nextOperand(argty, s);
        upvalues.add(arg);
      }
      TokenKind.RBRACE.next(s);

      final String callee = s.next();
      return new Closure(callee, upvalues);
    }

    public static String nextLabel(final Scanner s) {
      TokenKind.IDENT.next(s);
      return TokenKind.LOCAL.next(s);
    }

    public Lvalue nextMemory(final MirTy type, final Scanner s) {
      final String name = TokenKind.IDENT_HIDDEN.next(s);
      Lvalue mem = new Static(name, type);

      while (s.hasNext()) {
        if (TokenKind.DOT.nextIn(s)) {
          TokenKind.DOT.next(s);
          mem = new Field(mem, TokenKind.IDENT.next(s));
        } else if (TokenKind.LBRACKET.nextIn(s)) {
          TokenKind.LBRACKET.next(s);
          final MirArray atype = (MirArray) mem.getType();
          final Operand index = nextOperand(new IntTy(atype.indexBitLength()), s);
          mem = new Index(mem, index);
          TokenKind.RBRACKET.next(s);
        }
      }
      return mem;
    }

    public Store nextStore(final Scanner s) {
      if (s.hasNext("store")) {
        TokenKind.IDENT.next(s);

        final MirTy srcty = nextType(s);
        final Operand src = nextOperand(srcty, s);
        final MirTy memty = nextType(s);
        final Lvalue mem = nextMemory(memty, s);

        return new Store(mem, src);
      }
      return null;
    }

    public Instruction nextAssign(final Scanner s) {
      if (TokenKind.LOCAL.nextIn(s)) {
        final String name = TokenKind.LOCAL.next(s);
        TokenKind.EQUALS.next(s);

        if (s.hasNext("load")) {
          return nextLoad(name, s);
        }
        if (s.hasNext("call")) {
          return nextCall(name, s);
        }
        if (s.hasNext("Sext") || s.hasNext("Zext")) {
          return nextCast(name, s);
        }
        if (s.hasNext("Extract")) {
          return nextExtract(name, s);
        }
        if (s.hasNext("Concat")) {
          return nextConcat(name, s);
        }
        if (s.hasNext("Disclose")) {
          return nextDisclose(name, s);
        }

        final MirTy lhsty = nextType(s);
        final Lvalue lhs = newLocal(name, lhsty);

        final String insn = TokenKind.IDENT.next(s);
        final MirTy rhsty = nextType(s);
        final Operand op1 = nextOperand(rhsty, s);

        for (final UnOpcode opc : UnOpcode.values()) {
          if (opc.name().equals(insn)) {
            return new Assignment(lhs, opc.make(op1, null));
          }
        }

        final Operand op2 = nextOperand(rhsty, s);
        for (final BvOpcode opc : BvOpcode.values()) {
          if (opc.name().equals(insn)) {
            return new Assignment(lhs, opc.make(op1, op2));
          }
        }
        for (final CmpOpcode opc : CmpOpcode.values()) {
          if (opc.name().equals(insn)) {
            return new Assignment(lhs, opc.make(op1, op2));
          }
        }
        throw new IllegalArgumentException();
      }
      return null;
    }

    private Instruction nextDisclose(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);

      final MirTy lhsty = nextType(s);
      TokenKind.IDENT.next(s); // skip 'of'
      final MirTy rhsty = nextType(s);
      final Operand src = nextOperand(rhsty, s);

      final List<Constant> indices = new java.util.ArrayList<>();
      while (s.hasNext()) {
        final MirTy type = nextType(s);
        final Constant arg = nextConst(type, s);
        indices.add(arg);
      }

      final Local lhs = newLocal(id, lhsty);
      return new Disclose(lhs, src, indices);
    }

    private Instruction nextConcat(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy lhsty = nextType(s);
      final Lvalue lhs = newLocal(id, lhsty);

      final List<Operand> args = new java.util.ArrayList<>();
      while (s.hasNext()) {
        final MirTy type = nextType(s);
        final Operand arg = nextOperand(type, s);
        args.add(arg);
      }
      return new Concat(lhs, args);
    }

    private Instruction nextExtract(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy lhsty = nextType(s);
      final Lvalue lhs = newLocal(id, lhsty);

      TokenKind.IDENT.next(s); // of
      final MirTy rhsty = nextType(s);
      final Operand rhs = nextOperand(rhsty, s);

      final Operand hi = nextOperand(rhsty, s);
      final Operand lo = nextOperand(rhsty, s);

      return new Extract(lhs, rhs, lo, hi);
    }

    private Instruction nextCast(final String id, final Scanner s) {
      final String cast = TokenKind.IDENT.next(s);
      final MirTy rhsty = nextType(s);
      final Operand rhs = nextOperand(rhsty, s);
      TokenKind.IDENT.next(s);

      final MirTy lhsty = nextType(s);
      final Local lhs = newLocal(id, lhsty);

      if (cast.equals("Sext")) {
        return new Sext(lhs, rhs);
      }
      return new Zext(lhs, rhs);
    }

    private Load nextLoad(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);

      final MirTy dstty = nextType(s);
      final Local dst = newLocal(id, dstty);
      final MirTy memty = nextType(s);
      final Lvalue mem = nextMemory(memty, s);
      return new Load(mem, dst);
    }

    public Branch nextBranch(final Map<String, MirBlock> bbs, final Scanner s) {
      if (s.hasNext("br")) {
        TokenKind.IDENT.next(s);
        if (s.hasNext("label")) {
          final String label = nextLabel(s);
          return new Branch(bbs.get(label).bb);
        } else {
          final MirTy type = nextType(s);
          final Operand guard = nextOperand(type, s);
          final String takenLabel = nextLabel(s);
          final String otherLabel = nextLabel(s);

          return new Branch(guard, bbs.get(takenLabel).bb, bbs.get(otherLabel).bb);
        }
      }
      return null;
    }

    public Return nextReturn(final Scanner s) {
      if (s.hasNext("ret")) {
        TokenKind.IDENT.next(s);
        if (s.hasNext("void")) {
          return new Return(null);
        }
        final MirTy type = nextType(s);
        final Operand value = nextOperand(type, s);

        return new Return(value);
      }
      return null;
    }

    private Local newLocal(final String id, final MirTy type) {
      final int index = Integer.valueOf(id);
      final int size = locals.size();
      if (size <= index) {
        locals.addAll(Collections.nCopies(index - size, VoidTy.VALUE));
        locals.add(type);
      }
      return new Local(index, type);
    }
  }
}
