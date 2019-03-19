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

import ru.ispras.microtesk.Logger;

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
    final Map<String, Parser> parsers = standardParsers(blocks);

    for (final String line : lines) {
      if (line.matches("\\w+:")) {
        final String name = line.substring(0, line.indexOf(":"));
        block = blocks.get(name);
      } else {
        final String[] tokens = line.replaceAll("[,<>()]", "").trim().split("\\s+");
        final Parser parser = parsers.get(tokens[0]);
        if (parser != null) {
          block.append(parser.parse(tokens));
        } else {
          // TODO
        }
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

        return new Call(callee, args, null);
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

    Parser2(final Scanner s) {
      this.scanner = s;
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
        nextToken(TokenKind.LBRACE, s);

        final Map<String, TyRef> fields =
            nextFields(s, new java.util.LinkedHashMap<String, TyRef>());

        return new MirStruct(fields);
      }
      return null;
    }

    private static Map<String, TyRef> nextFields(final Scanner s, final Map<String, TyRef> fields) {
      if (nextToken(TokenKind.RBRACE, s) != null) {
        return fields;
      }
      final MirTy type = nextType(s);
      final String name = TokenKind.IDENT.next(s);

      fields.put(name, new TyRef(type));
      return nextFields(s, fields);
    }

    public static Call nextCall(final String target, final Scanner s) {
      s.skip("call");
      final MirTy retty = nextType(s);
      final Local ret;
      if (target.isEmpty()) {
        ret = null;
      } else {
        ret = new Local(Integer.valueOf(target), retty);
      }

      final String calleeName = TokenKind.LOCAL.next(s);
      TokenKind.LPAREN.next(s);

      final List<Operand> args = nextArguments(s, new java.util.ArrayList<Operand>());
      final List<MirTy> params = new java.util.ArrayList<>(args.size());
      for (final Operand arg : args) {
        params.add(arg.getType());
      }
      final Operand callee =
          new Local(Integer.valueOf(calleeName), new FuncTy(retty, params));

      return new Call(callee, args, ret);
    }

    public static List<Operand> nextArguments(final Scanner s, final List<Operand> args) {
      if (nextToken(TokenKind.RPAREN, s) != null) {
        return args;
      }
      final MirTy type = nextType(s);
      final Operand arg = nextOperand(type, s);

      args.add(arg);
      return nextArguments(s, args);
    }

    public static Operand nextOperand(final MirTy type, final Scanner s) {
      if (TokenKind.LOCAL.nextIn(s)) {
        return new Local(Integer.valueOf(TokenKind.LOCAL.next(s)), type);
      }
      return new Constant(type.getSize(), Integer.valueOf(s.next("\\d+")));
    }

    public static String nextLabel(final Scanner s) {
      s.skip("label");
      return TokenKind.LOCAL.next(s);
    }
  }
}
