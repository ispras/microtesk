package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GlobalNumbering extends Pass {
  private List<Node> blocks;
  private Map<Node, Node> forkJoin;

  private final List<Map<String, Def>> defs = new java.util.ArrayList<>();
  private final Map<String, Integer> versions = new java.util.HashMap<>();

  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);

    init(ctx);
    run();

    for (final Node node : blocks) {
      for (final Instruction insn : node.bb.insns) {
        if (insn instanceof Phi) {
          final Phi phi = (Phi) insn;
          phi.value = compress(getVariants(phi.target, node));
        }
      }
    }

    return ctx;
  }

  private void init(final MirContext ctx) {
    this.blocks = breadthFirst(ctx);
    this.forkJoin = mapForkJoin(blocks);
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
    final List<Def> variants = getVariants(mem, node);
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

  private List<Def> getVariants(final Static mem, final Node node) {
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
    return variants;
  }

  private Ite compress(final List<Def> defs) {
    final Map<Def, ControlDep> deps = new java.util.IdentityHashMap<>();
    for (final Def def : defs) {
      deps.put(def, listControlDeps(def));
    }
    return compressRec(deps.keySet(), deps);
  }

  private Ite compressRec(final Collection<Def> defs, final Map<Def, ControlDep> depsAll) {
    final Map<Def, ControlDep> deps = new java.util.IdentityHashMap<>(depsAll);
    deps.keySet().retainAll(defs);

    final Node fork = searchFork(deps.values());
    final List<Def> takenList = new java.util.ArrayList<>();
    final List<Def> otherList = new java.util.ArrayList<>();

    Def counterDef = null;
    for (final ControlDep dep : deps.values()) {
      final int index = dep.path.indexOf(fork);
      if (index == 0) {
        counterDef = dep.def;
      } else if (dep.pathTaken.get(index - 1)) {
        takenList.add(dep.def);
      } else {
        otherList.add(dep.def);
      }
      dep.limit = index;
    }
    if (takenList.isEmpty()) {
      takenList.add(counterDef);
    } else if (otherList.isEmpty()) {
      otherList.add(counterDef);
    }
    final Operand taken = compressDef(takenList, depsAll);
    final Operand other = compressDef(otherList, depsAll);
    final Branch br = (Branch) lastOf(fork.bb.insns);

    return new Ite(br.guard, taken, other);
  }

  private Operand compressDef(final Collection<Def> defs, final Map<Def, ControlDep> deps) {
    if (defs.size() == 1) {
      return defs.iterator().next().mem;
    }
    return compressRec(defs, deps);
  }

  private Node searchFork(final Collection<ControlDep> deps) {
    final ControlDep sample = deps.iterator().next();
    for (int i = 0; i < sample.limit; ++i) {
      final Node fork = sample.path.get(i);
      if (allContains(deps, fork)) {
        return fork;
      }
    }
    return null;
  }

  private static boolean allContains(final Collection<ControlDep> deps, final Node node) {
    for (final ControlDep dep : deps) {
      if (!dep.path.contains(node)) {
        return false;
      }
    }
    return true;
  }

  private ControlDep listControlDeps(final Def def) {
    final ControlDep dep = new ControlDep(def);

    Node node = def.bb;
    while (!node.pred.isEmpty()) {
      final Node pred = node.pred.get(0);
      if (pred.succ.size() > 1) {
        final Node join = forkJoin.get(pred);
        final int index = dep.path.indexOf(join);
        if (index >= 0) {
          dep.removeTail(index);
        } else {
          dep.add(pred, node);
        }
      } else if (pred.pred.size() > 1) {
        dep.add(pred, node);
      }
      node = pred;
    }
    dep.limit = dep.path.size();

    return dep;
  }

  final static class ControlDep {
    final Def def;
    final List<Node> path = new java.util.ArrayList<>();
    final List<Boolean> pathTaken = new java.util.ArrayList<>();

    int limit;

    ControlDep(final Def def) {
      this.def = def;
      path.add(def.bb);
    }

    void add(final Node dep, final Node src) {
      path.add(dep);
      pathTaken.add(dep.succ.get(0) == src);
    }

    void removeTail(final int index) {
      path.subList(index, path.size()).clear();
      pathTaken.subList(index, pathTaken.size()).clear();
    }
  }

  private static boolean isComplete(final Collection<Ite> items) {
    for (final Ite ite : items) {
      if (ite.taken == null || ite.other == null) {
        return false;
      }
    }
    return true;
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

  private static Map<Node, Node> mapForkJoin(final Collection<Node> blocks) {
    final Map<Node, Node> forkJoin = new java.util.IdentityHashMap<>();
    for (final Node fork : blocks) {
      final Node join = searchJoin(fork);
      if (join != null) {
        forkJoin.put(fork, join);
      }
    }
    return forkJoin;
  }

  private static Node searchJoin(final Node fork) {
    final List<Node> succ = fork.succ;
    if (succ.size() > 1) {
      final List<Node> pathTaken =
        depthFirstPath(succ.get(0), Collections.<Node>emptyList());
      final List<Node> pathOther = depthFirstPath(succ.get(1), pathTaken);

      final Node join = lastOf(pathOther);
      if (pathTaken.contains(join)) {
        return join;
      }
    }
    return null;
  }

  private static List<Node> depthFirstPath(
      final Node src, final Collection<Node> observed) {
    final List<Node> path = new java.util.ArrayList<>();

    Node node = src;
    for (List<Node> succ = node.succ;
        !succ.isEmpty() && !observed.contains(node);
        succ = node.succ) {
      path.add(node);
      node = succ.get(0);
    }
    path.add(node);

    return path;
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
    final Operand guard;
    final Operand taken;
    final Operand other;

    public Ite(Operand guard, Operand taken, Operand other) {
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
    Ite value = null;

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
