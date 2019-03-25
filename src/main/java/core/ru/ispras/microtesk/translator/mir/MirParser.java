package ru.ispras.microtesk.translator.mir;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import ru.ispras.castle.util.Logger;

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
    final MirContext ctx = new MirContext();
    final Map<String, MirBlock> blocks = new java.util.HashMap<>();

    for (final String line : lines) {
      if (line.matches("\\w+:")) {
        final String name = line.substring(0, line.indexOf(":"));
        blocks.put(name, ctx.newBlock());
      }
    }
    MirBlock block = null;

    for (final String line : lines) {
      if (line.matches("\\w+:")) {
        final String name = line.substring(0, line.indexOf(":"));
        block = blocks.get(name);
      } else {
        final String input =
            line.replaceAll("[,()<>]", "").replaceAll("([\\[\\]\\{\\}])", " $1 ").trim();
        final Parser2 parser = new Parser2(new Scanner(input), blocks);
        block.append(parser.nextInsn());
      }
    }
    return ctx;
  }

  private static Map<String, Parser> standardParsers(final Map<String, MirBlock> blocks) {
    final Map<String, Parser> parsers = new java.util.HashMap<>();
    parsers.put("ret", new Parser() {
      @Override
      public Instruction parse(final String[] tokens) {
        final MirTy ty = typeOf(tokens[1]);
        if (ty != Types.VOID) {
          return new Return(valueOf(ty, tokens[2]));
        }
        return new Return(null);
      }
    });
    parsers.put("br", new Parser() {
      @Override
      public Instruction parse(final String[] tokens) {
        if (tokens[1].equals("label")) {
          return new Branch(blocks.get(labelName(tokens[2])).bb);
        }
        final MirTy condTy = typeOf(tokens[1]);
        final Operand cond = valueOf(condTy, tokens[2]);
        final MirBlock taken = blocks.get(labelName(tokens[4]));
        final MirBlock other = blocks.get(labelName(tokens[6]));

        return new Branch(cond, taken.bb, other.bb);
      }
    });
    parsers.put("call", new Parser() {
      @Override
      public Instruction parse(final String[] tokens) {
        final MirTy retty = typeOf(tokens[1]);
        final List<MirTy> params = new java.util.ArrayList<>();
        final List<Operand> args = new java.util.ArrayList<>();
        for (int i = 3; i < tokens.length; i += 2) {
          final MirTy ty = typeOf(tokens[i]);
          final Operand arg = valueOf(ty, tokens[i + 1]);
          params.add(ty);
          args.add(arg);
        }
        final MirTy ty = new FuncTy(retty, params);
        final Operand callee = valueOf(ty, tokens[2]);

        return new Call(callee, "", args, null);
      }
    });
    parsers.put("store", new Parser() {
      @Override
      public Instruction parse(final String[] tokens) {
        final MirTy srcTy = typeOf(tokens[1]);
        final Operand src = valueOf(srcTy, tokens[2]);
        final MirTy dstTy = typeOf(tokens[3]);
        final Lvalue dst = memoryOf(dstTy, tokens[4]);

        return new Store(dst, src);
      }
    });
    return parsers;
  }

  private static String labelName(final String s) {
    return s.substring(1);
  }

  private static MirTy typeOf(final String s) {
    return Types.valueOf(s);
  }

  private static Operand valueOf(final MirTy ty, final String s) {
    if (s.startsWith("%")) {
      return new Local(Integer.valueOf(s.substring(1)), ty);
    }
    return new Constant(ty.getSize(), Integer.valueOf(s));
  }

  private static Lvalue memoryOf(final MirTy ty, final String s) {
    return null;
  }

  private interface Parser {
    Instruction parse(String[] tokens);
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

    Parser2(final Scanner s, final Map<String, MirBlock> blocks) {
      this.scanner = s;
      this.blocks = blocks;
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
      for (final Token token = nextToken(TokenKind.IDENT, s); token != null; ) {
        return Types.valueOf(token.value);
      }
      throw new IllegalStateException();
    }

    private static MirArray nextArray(final Scanner s) {
      if (TokenKind.LBRACKET.nextIn(s)) {
        nextToken(TokenKind.LBRACKET, s);
        final int size = s.nextInt();
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

    public static Call nextCall(final String target, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy retty = nextType(s);
      final Local ret;
      if (target.isEmpty()) {
        ret = null;
      } else {
        ret = new Local(Integer.valueOf(target), retty);
      }

      final String method = s.next();
      final String calleeName = TokenKind.LOCAL.next(s);

      final List<Operand> args = new java.util.ArrayList<>();
      final List<MirTy> params = new java.util.ArrayList<>();
      while (s.hasNext()) {
        final MirTy type = nextType(s);
        final Operand arg = nextOperand(type, s);
        params.add(type);
        args.add(arg);
      }

      final Operand callee =
          new Local(Integer.valueOf(calleeName), new FuncTy(retty, params));

      return new Call(callee, method, args, ret);
    }

    public static Operand nextOperand(final MirTy type, final Scanner s) {
      if (TokenKind.LOCAL.nextIn(s)) {
        return new Local(Integer.valueOf(TokenKind.LOCAL.next(s)), type);
      }
      try {
        return new Constant(type.getSize(), s.nextInt());
      } catch (final java.util.InputMismatchException e) { // FIXME
        return new Constant(type.getSize(), 0);
      }
    }

    public static String nextLabel(final Scanner s) {
      TokenKind.IDENT.next(s);
      return TokenKind.LOCAL.next(s);
    }

    public static Lvalue nextMemory(final MirTy type, final Scanner s) {
      final String name = TokenKind.IDENT.next(s);
      Lvalue mem = new Static(name, type);

      while (s.hasNext()) {
        if (TokenKind.DOT.nextIn(s)) {
          TokenKind.DOT.next(s);
          mem = new Field(mem, TokenKind.IDENT.next(s));
        } else if (TokenKind.LBRACKET.nextIn(s)) {
          TokenKind.LBRACKET.next(s);
          final Operand index = nextOperand(new IntTy(64), s);
          mem = new Index(mem, index);
          TokenKind.RBRACKET.next(s);
        }
      }
      return mem;
    }

    public static Store nextStore(final Scanner s) {
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

    public static Instruction nextAssign(final Scanner s) {
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

        final MirTy lhsty = nextType(s);
        final Lvalue lhs = new Local(Integer.valueOf(name), lhsty);

        final String insn = TokenKind.IDENT.next(s);
        final MirTy rhsty = nextType(s);
        final Operand op1 = nextOperand(rhsty, s);
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

    private static Instruction nextConcat(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy lhsty = nextType(s);
      final Lvalue lhs = new Local(Integer.valueOf(id), lhsty);

      final List<Operand> args = new java.util.ArrayList<>();
      while (s.hasNext()) {
        final MirTy type = nextType(s);
        final Operand arg = nextOperand(type, s);
        args.add(arg);
      }
      return new Concat(lhs, args);
    }

    private static Instruction nextExtract(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);
      final MirTy lhsty = nextType(s);
      final Lvalue lhs = new Local(Integer.valueOf(id), lhsty);

      TokenKind.IDENT.next(s); // of
      final MirTy rhsty = nextType(s);
      final Operand rhs = nextOperand(rhsty, s);

      final Operand hi = nextOperand(rhsty, s);
      final Operand lo = nextOperand(rhsty, s);

      return new Extract(lhs, rhs, lo, hi);
    }

    private static Instruction nextCast(final String id, final Scanner s) {
      final String cast = TokenKind.IDENT.next(s);
      final MirTy rhsty = nextType(s);
      final Operand rhs = nextOperand(rhsty, s);
      TokenKind.IDENT.next(s);

      final MirTy lhsty = nextType(s);
      final Local lhs = new Local(Integer.valueOf(id), lhsty);

      if (cast.equals("Sext")) {
        return new Sext(lhs, rhs);
      }
      return new Zext(lhs, rhs);
    }

    private static Load nextLoad(final String id, final Scanner s) {
      TokenKind.IDENT.next(s);

      final MirTy dstty = nextType(s);
      final Local dst = new Local(Integer.valueOf(id), dstty);
      final MirTy memty = nextType(s);
      final Lvalue mem = nextMemory(memty, s);
      return new Load(mem, dst);
    }

    public static Branch nextBranch(final Map<String, MirBlock> bbs, final Scanner s) {
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

    public static Return nextReturn(final Scanner s) {
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
  }
}
