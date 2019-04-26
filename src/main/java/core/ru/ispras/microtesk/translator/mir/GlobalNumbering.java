package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GlobalNumbering extends Pass {
  private List<Node> blocks;
  private final List<Map<String, Instruction>> defs = new java.util.ArrayList<>();
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
      defs.add(new java.util.HashMap<String, Instruction>());
    }
  }

  private void run() {
    for (final Node node : blocks) {
      final List<Instruction> insns = node.bb.insns;
      for (int i = 0; i < insns.size(); ++i) {
        final Instruction insn = insns.get(i);
        if (insn instanceof Store) {
          final Store store = (Store) insn;
          final int version = incrementVersion(getMemory(store.target));

          final Store def = new Store(update(store.target, version), store.source);
          insns.set(i, def);
          define(def, node);
        } else if (insn instanceof Load) {
          final Load load = (Load) insn;
          final Static mem = getMemory(load.source);
          final Static def = reload(mem, node);

          insns.set(insns.indexOf(load), new Load(update(load.source, def.version), load.target));
        }
      }
    }
  }

  private Static reload(final Static mem, final Node node) {
    final Instruction insn = getDefs(node).get(mem.name);
    if (insn == null ) {
      return reloadRecursive(mem, node);
    } else if (insn instanceof Store) {
      return getMemory(((Store) insn).target);
    } else {
      return ((Phi) insn).target;
    }
  }

  private Static reloadRecursive(final Static mem, final Node node) {
    if (node.pred.isEmpty()) {
      return new Static(mem.name, 1, mem.getType());
    }
    final List<Static> variants = new java.util.ArrayList<>();
    for (final Node pred : node.pred) {
      variants.add(reload(mem, pred));
    }
    for (int i = 0; i < variants.size(); ++i) {
      final Static origin = variants.get(i);
      final Iterator<Static> it = variants.subList(i + 1, variants.size()).iterator();
      while (it.hasNext()) {
        final Static sample = it.next();
        if (sample.version == origin.version) {
          it.remove();
        }
      }
    }
    if (variants.size() == 1) {
      return variants.get(0);
    }
    final Static newval = new Static(mem.name, incrementVersion(mem), mem.getType());
    final Phi phi = new Phi(newval, variants);
    node.bb.insns.add(0, phi);
    define(phi, node);

    return newval;
  }

  private void define(final Instruction insn, final Node node) {
    final Static mem;
    if (insn instanceof Store) {
      mem = getMemory(((Store) insn).target);
    } else {
      mem = getMemory(((Phi) insn).target);
    }
    getDefs(node).put(mem.name, insn);
  }

  private int incrementVersion(final Static mem) {
    final int ver = (versions.containsKey(mem.name)) ? versions.get(mem.name) + 1 : 2;
    versions.put(mem.name, ver);

    return ver;
  }

  private Map<String, Instruction> getDefs(final Node node) {
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
}
