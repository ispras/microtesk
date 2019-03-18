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

  private enum Token {
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
    LOCAL("\\%\\w+"),
    IDENT("\\w+"),
    EOF("");
    
    private final List<Pattern> patterns;

    private Token(final String s) {
      this.patterns = Collections.singletonList(Pattern.compile(s));
    }
  }

  private interface Rule {
    Rule lookAhead(Scanner s);
    List<Object> parse(Scanner s);
  }

  private static final class MatchRule implements Rule {
    public final List<Rule> rules = new java.util.ArrayList<>();

    @Override
    public Rule lookAhead(final Scanner s) {
      if (rules.get(0).lookAhead(s) != null) {
        return this;
      }
      return null;
    }

    @Override
    public List<Object> parse(final Scanner s) {
      final List<Object> list = new java.util.ArrayList<>(rules.size());
      for (final Rule r : rules) {
        list.addAll(r.lookAhead(s).parse(s));
      }
      return list;
    }
  }

  private static final class SelectRule implements Rule {
    public final List<Rule> rules = new java.util.ArrayList<>();

    @Override
    public Rule lookAhead(final Scanner s) {
      for (final Rule variant : rules) {
        for (final Rule rule = variant.lookAhead(s); rule != null; ) {
          return rule;
        }
      }
      return null;
    }

    @Override
    public List<Object> parse(final Scanner s) {
      for (final Rule rule = lookAhead(s); rule != null; ) {
        return rule.parse(s);
      }
      throw new IllegalArgumentException();
    }
  }

  private enum MirRule implements Rule {
    RHS,
    LABEL("label", Token.IDENT),
    OPERAND(select(Token.LOCAL, "\\d+")),
    TYPE {
      @Override
      public Rule lookAhead(final Scanner s) {
        if (rule.lookAhead(s) != null) {
          return this;
        }
        return null;
      }

      @Override
      public List<Object> parse(final Scanner s) {
        final List<Object> result = rule.lookAhead(s).parse(s);
        for (final Object o = result.get(0); o instanceof String; ) {
          return Collections.<Object>singletonList(Types.valueOf((String) o));
        }
        return result;
      }
    },
    ARGS_RPAREN,

    ASSIGN(Token.LOCAL, Token.EQUALS, RHS),
    BRANCH("br", select(LABEL, match(TYPE, OPERAND, LABEL, LABEL))),
    RET("ret", select("void", match(TYPE, OPERAND))),
    CALL("call", TYPE, Token.LPAREN, ARGS_RPAREN),

    TYPE_ARRAY(Token.LBRACKET, "\\d+", TYPE, Token.RBRACKET) {
      @Override
      public Rule lookAhead(final Scanner s) {
        if (rule.lookAhead(s) != null) {
          return this;
        }
        return null;
      }

      @Override
      public List<Object> parse(final Scanner s) {
        final List<Object> result = rule.parse(s);
        final int size = Integer.valueOf((String) result.get(1));
        final MirTy type = (MirTy) result.get(2);

        return Collections.<Object>singletonList(new MirArray(size, new TyRef(type)));
      }
    },
    FIELDS_RBRACE,
    TYPE_STRUCT(Token.LBRACE, FIELDS_RBRACE),
    MEMORY_TAIL,
    MEMORY(Token.IDENT, MEMORY_TAIL),
    INDEX(Token.LBRACKET, OPERAND, Token.RBRACKET, MEMORY_TAIL),

    STORE("store", TYPE, OPERAND, TYPE, MEMORY);

    static {
      TYPE.rule           = select("void", "i\\d+", "f\\d+", TYPE_ARRAY, TYPE_STRUCT);
      ARGS_RPAREN.rule    = select(Token.RPAREN, match(TYPE, OPERAND, ARGS_RPAREN));
      FIELDS_RBRACE.rule  = select(Token.RBRACE, match(TYPE, Token.IDENT, FIELDS_RBRACE));
      MEMORY_TAIL.rule    = select(Token.EOF, match(Token.DOT, MEMORY), INDEX);
    }

    private static Rule select(Object... matchers) {
      return new SelectRule();
    }

    private static Rule match(Object... matchers) {
      return new MatchRule();
    }

    @Override
    public Rule lookAhead(final Scanner s) {
      return rule.lookAhead(s);
    }

    @Override
    public List<Object> parse(final Scanner s) {
      return rule.parse(s);
    }

    MirRule(Object... matchers) {
      this.rule = match(matchers);
    }

    protected Rule rule = null;
  }
}
