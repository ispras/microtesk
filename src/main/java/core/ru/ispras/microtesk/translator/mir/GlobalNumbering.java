package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GlobalNumbering extends Pass {
  private List<Node> blocks;

  private final List<Map<String, Def>> defs = new java.util.ArrayList<>();
  private final Map<String, Integer> versions = new java.util.HashMap<>();

  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);

    init(ctx);
    run();

    return ctx;
  }

  private void init(final MirContext ctx) {
    this.blocks = breadthFirst(ctx);
    this.defs.clear();
    this.versions.clear();

    for (int i = 0; i< blocks.size(); ++i) {
      defs.add(new java.util.HashMap<String, Def>());
    }
  }

  private void run() {
    for (final Node node : blocks) {
      final List<Instruction> insns = node.bb.insns;
      for (int i = 0; i < insns.size(); ++i) {
        final Instruction insn = insns.get(i);
        if (insn instanceof Store) {
          final Store store = (Store) insn;
          final Static current = reload(store.target, node).mem;

          final Store update =
              new Store(update(store.target, current.version), store.source);
          final SsaStore def = new SsaStore(incrementVersion(current), update);
          insns.set(i, def);
          define(def.target, def, node);
        } else if (insn instanceof Load) {
          final Load load = (Load) insn;
          final Static def = reload(load.source, node).mem;

          insns.set(insns.indexOf(load), new Load(update(load.source, def.version), load.target));
        }
      }
    }
  }

  private Def reload(final Lvalue lval, final Node node) {
    return reload(getMemory(lval), node);
  }

  private Def reload(final Static mem, final Node node) {
    final Def def = getDefs(node).get(mem.name);
    if (def == null ) {
      return reloadRecursive(mem, node);
    }
    return def;
  }

  private Def reloadRecursive(final Static mem, final Node node) {
    if (node.pred.isEmpty()) {
      return new Def(new Static(mem.name, 1, mem.getType()), null, node);
    }
    final List<Def> variants = new java.util.ArrayList<>();
    for (final Node pred : node.pred) {
      variants.add(reload(mem, pred));
    }
    for (int i = 0; i < variants.size(); ++i) {
      final Static origin = variants.get(i).mem;
      final Iterator<Def> it = variants.subList(i + 1, variants.size()).iterator();
      while (it.hasNext()) {
        final Static sample = it.next().mem;
        if (sample.version == origin.version) {
          it.remove();
        }
      }
    }
    if (variants.size() == 1) {
      return variants.get(0);
    }
    final List<Static> memvars = new java.util.ArrayList<>(variants.size());
    for (final Def def : variants) {
      memvars.add(def.mem);
    }
    final Phi phi = new Phi(incrementVersion(mem), memvars);
    node.bb.insns.add(0, phi);

    return define(phi.target, phi, node);
  }

  private Def define(final Static mem, final Instruction insn, final Node node) {
    final Def def = new Def(mem, insn, node);
    getDefs(node).put(mem.name, def);
    return def;
  }

  final static class Def {
    Static mem;
    Instruction insn;
    Node bb;

    public Def(Static mem, Instruction insn, Node bb) {
      this.mem = mem;
      this.insn = insn;
      this.bb = bb;
    }
  }

  private Static incrementVersion(final Static mem) {
    final int ver = (versions.containsKey(mem.name)) ? versions.get(mem.name) + 1 : 2;
    versions.put(mem.name, ver);

    return new Static(mem.name, ver, mem.getType());
  }

  private Map<String, Def> getDefs(final Node node) {
    return defs.get(blocks.indexOf(node));
  }

  private static Static getMemory(final Lvalue lval) {
    if (lval instanceof Index) {
      return getMemory(((Index) lval).base);
    }
    if (lval instanceof Field) {
      return getMemory(((Field) lval).base);
    }
    return (Static) lval;
  }

  private static Lvalue update(final Lvalue lval, final int v) {
    if (lval instanceof Index) {
      final Index index = (Index) lval;
      return new Index(update(index.base, v), index.index);
    }
    if (lval instanceof Field) {
      final Field field = (Field) lval;
      return new Field(update(field.base, v), field.name);
    }
    final Static mem = (Static) lval;
    return new Static(mem.name, v, mem.getType());
  }

  private static List<Node> breadthFirst(final MirContext ctx) {
    final Map<BasicBlock, Node> map = new java.util.IdentityHashMap<>();
    for (final BasicBlock bb : ctx.blocks) {
      final Node node = new Node(bb);
      map.put(bb, node);
    }
    for (final Node node : map.values()) {
      final List<BasicBlock> succList = targetsOf(node.bb);
      for (final BasicBlock bb : succList) {
        final Node succ = map.get(bb);
        node.succ.add(succ);
        succ.pred.add(node);
      }
    }
    final List<Node> nodes = new java.util.ArrayList<>();
    nodes.add(map.get(ctx.blocks.get(0)));
    for (int i = 0; i < nodes.size(); ++i) {
      final Node node = nodes.get(i);
      for (final Node succ : node.succ) {
        if (!nodes.contains(succ) && nodes.containsAll(succ.pred)) {
          nodes.add(succ);
        }
      }
    }
    return nodes;
  }

  private static List<BasicBlock> targetsOf(final BasicBlock bb) {
    final Instruction insn = lastOf(bb.insns);
    if (insn instanceof Branch) {
      return ((Branch) insn).successors;
    }
    return Collections.emptyList();
  }

  private static <T> T lastOf(final List<T> list) {
    return list.get(list.size() - 1);
  }

  static class Node {
    public final List<Node> pred = new java.util.ArrayList<>();
    public final List<Node> succ = new java.util.ArrayList<>();
    public final BasicBlock bb;

    Node(final BasicBlock bb) {
      this.bb = bb;
    }
  }

  static class Ite implements Operand {
    final Local guard;
    final Operand taken;
    final Operand other;

    public Ite(Local guard, Operand taken, Operand other) {
      this.guard = guard;
      this.taken = taken;
      this.other = other;
    }

    @Override
    public MirTy getType() {
      return taken.getType();
    }
  }

  static class Phi implements Instruction {
    final Static target;
    final List<Static> values;

    public Phi(final Static target, final List<Static> values) {
      this.target = target;
      this.values = values;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }

  static class SsaStore implements Instruction {
    final Static target;
    final Store origin;

    public SsaStore(Static target, Store origin) {
      this.target = target;
      this.origin = origin;
    }

    @Override
    public void accept(final InsnVisitor visitor) {
      visitor.visit(this);
    }
  }
}
