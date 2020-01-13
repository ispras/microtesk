package ru.ispras.microtesk.translator.mir;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.ispras.microtesk.translator.mir.Instruction.Branch;
import static ru.ispras.microtesk.translator.mir.Instruction.Load;
import static ru.ispras.microtesk.translator.mir.Instruction.Store;

public class GlobalNumbering extends Pass {
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    constructSSA(ctx);
    return ctx;
  }

  private void replacePhi(final BasicBlock bb, final GContext ctx) {
    for (int i = 0; i < bb.insns.size(); ++i) {
      final Instruction insn = bb.insns.get(i);
      if (insn instanceof Phi) {
        final Phi phi = (Phi) insn;
        final Operand value = compressNet(phi.values, bb, ctx);
        if (value instanceof Ite) {
          phi.value = (Ite) value;
        } else {
          phi.value = new Ite(new Constant(1, 1), value, value);
        }
      }
    }
  }

  private Operand compressNet(
      final List<? extends Operand> values, final BasicBlock sink, final GContext ctx) {

    final BasicBlock idom = ctx.dom.idom.get(sink);
    final Map<BasicBlock, GsaInfo> forkInfo = new java.util.HashMap<>();

    final List<BasicBlock> worklist = new java.util.ArrayList<>(values.size());
    final List<Operand> datalist = new java.util.ArrayList<>(values.size());

    worklist.addAll(ctx.preds.get(sink));
    datalist.addAll(values);

    int iterno = 0;
    while (!worklist.isEmpty()) {
      final BasicBlock bb = Lists.removeLast(worklist);
      final Operand value = Lists.removeLast(datalist);

      if (!idom.equals(bb)) {
        final List<BasicBlock> preds = ctx.preds.get(bb);
        for (final BasicBlock pred : preds) {
          if (ctx.mirNodes.successorsOf(pred, ctx.mir).size() > 1) {
            GsaInfo info = forkInfo.get(pred);
            if (info == null) {
              info = new GsaInfo();
              forkInfo.put(pred, info);
            }
            final Branch br = (Branch) Lists.lastOf(pred.insns);
            if (br.other.equals(bb)) {
              info.valueOther = value;
            } else {
              info.valueTaken = value;
            }
            if (info.valueTaken != null && info.valueOther != null) {
              worklist.add(pred);
              datalist.add(gsaCompress(br.guard, info));
            }
          } else {
            worklist.add(pred);
            datalist.add(value);
          }
        }
      } else {
        return value;
      }
    }
    throw new IllegalStateException();
  }

  private static Operand gsaCompress(final Operand guard, final GsaInfo info) {
    if (info.valueTaken.equals(info.valueOther)) {
      return info.valueTaken;
    }
    return new Ite(guard, info.valueTaken, info.valueOther);
  }

  private static final class GsaInfo {
    Operand valueTaken;
    Operand valueOther;
  }

  private void constructSSA(final MirContext mir) {
    varInfo.clear();

    final GContext ctx = new GContext(mir);

    insertPhi(ctx);
    searchRename(mir.blocks.get(0), ctx);
    for (final BasicBlock bb : mir.blocks) {
      replacePhi(bb, ctx);
    }
  }

  private static final class GContext {
    final MirContext mir;
    final MirNodes mirNodes = new MirNodes();
    final Map<BasicBlock, List<BasicBlock>> preds;
    final DominanceTree<BasicBlock, MirContext> dom;

    GContext(final MirContext mir) {
      this.mir = mir;
      this.preds = collectPred(mir, mirNodes);
      this.dom = LengauerTarjanDom.create(mirNodes).compute(mirNodes.entry, mir);
      dom.calculateFrontiers();
    }
  }

  private static Map<BasicBlock, List<BasicBlock>> collectPred(
      final MirContext mir, final MirNodes mirNodes) {
    final Map<BasicBlock, List<BasicBlock>> preds = new java.util.HashMap<>();

    preds.put(mirNodes.entry, Collections.<BasicBlock>emptyList());
    for (final BasicBlock bb : mirNodes.nodesOf(mir)) {
      for (final BasicBlock succ : mirNodes.successorsOf(bb, mir)) {
        List<BasicBlock> pred = preds.get(succ);
        if (pred == null) {
          pred = new java.util.ArrayList<>();
          preds.put(succ, pred);
        }
        pred.add(bb);
      }
    }
    return preds;
  }

  private void searchRename(final BasicBlock bb, final GContext ctx) {
    for (int i = 0; i < bb.insns.size(); ++i) {
      final Instruction insn = bb.insns.get(i);
      if (insn instanceof Load) {
        final Load origin = (Load) insn;
        final VarInfo info = getInfo(getMemory(origin.source));
        bb.insns.set(i, new Load(update(origin.source, info.current()), origin.target));
      } else if (insn instanceof Store) {
        final Store origin = (Store) insn;
        final Static base = getMemory(origin.target);
        final VarInfo info = getInfo(base);

        final Store store =
          new Store(update(origin.target, info.current()), origin.source);
        final SsaStore upd =
          new SsaStore((Static) update(base, info.assign()), store);
        bb.insns.set(i, upd);
      } else if (insn instanceof Phi) {
        final Phi origin = (Phi) insn;
        final Static mem = origin.target;
        final Phi phi = new Phi((Static) update(mem, getInfo(mem).assign()), origin.values);
        bb.insns.set(i, phi);
      }
    }
    for (final BasicBlock succ : ctx.mirNodes.successorsOf(bb, ctx.mir)) {
      final int index = ctx.preds.get(succ).indexOf(bb);
      for (int i = 0; i < succ.insns.size(); ++i) {
        final Instruction insn = succ.insns.get(i);
        if (insn instanceof Phi) {
          final Phi phi = (Phi) insn;
          final VarInfo info = getInfo(phi.values.get(index));
          phi.values.set(index, (Static) update(phi.target, info.current()));
        }
      }
    }
    if (ctx.dom.children.containsKey(bb)) {
      for (final BasicBlock child : ctx.dom.children.get(bb)) {
        searchRename(child, ctx);
      }
    }
    for (final Instruction insn : bb.insns) {
      if (insn instanceof SsaStore) {
        final SsaStore origin = (SsaStore) insn;
        getInfo(origin.target).fallback();
      } else if (insn instanceof Phi) {
        final Phi origin = (Phi) insn;
        getInfo(origin.target).fallback();
      }
    }
  }

  private VarInfo getInfo(final Static mem) {
    VarInfo info = varInfo.get(mem.name);
    if (info == null) {
      info = new VarInfo();
      varInfo.put(mem.name, info);
    }
    return info;
  }

  final Map<String, VarInfo> varInfo = new java.util.HashMap<>();

  private static final class VarInfo {
    private final List<Integer> version = new java.util.ArrayList<>();
    private int nassigned = 0;

    VarInfo() { assign(); }

    int assign() {
      final int value = ++nassigned;
      version.add(value);
      return value;
    }

    int current() {
      return Lists.lastOf(version);
    }

    void fallback() {
      version.remove(version.size() - 1);
    }
  }

  private void insertPhi(final GContext ctx) {
    final Map<BasicBlock, Integer> queuedAt = new java.util.HashMap<>();
    final Map<BasicBlock, Integer> placedAt = new java.util.HashMap<>();
    queuedAt.put(ctx.mirNodes.entry, 0);
    queuedAt.put(ctx.mirNodes.exit, 0);
    placedAt.put(ctx.mirNodes.entry, 0);
    placedAt.put(ctx.mirNodes.exit, 0);
    for (final BasicBlock bb : ctx.mir.blocks) {
      queuedAt.put(bb, 0);
      placedAt.put(bb, 0);
    }
    int iterno = 0;
    final Set<BasicBlock> queue = new java.util.LinkedHashSet<>();
    final Map<Static, List<DefLoc>> vardefs = collectAssignments(ctx.mir.blocks);
    for (final Static mem : vardefs.keySet()) {
      ++iterno;
      final List<DefLoc> defs = vardefs.get(mem);
      queuedAt.put(ctx.mirNodes.entry, iterno);
      queue.add(ctx.mirNodes.entry);
      for (final DefLoc def : defs) {
        queuedAt.put(def.bb, iterno);
        queue.add(def.bb);
      }
      while (!queue.isEmpty()) {
        final BasicBlock bb = queue.iterator().next();
        queue.remove(bb);

        for (final BasicBlock dfb : ctx.dom.frontiers.get(bb)) {
          if (placedAt.get(dfb) < iterno) {
            dfb.insns.add(0, newPhi(mem, ctx.preds.get(dfb).size()));
            placedAt.put(dfb, iterno);
            if (queuedAt.get(dfb) < iterno) {
              queuedAt.put(dfb, iterno);
              queue.add(dfb);
            }
          }
        }
      }
    }
  }

  private static Phi newPhi(final Static mem, int n) {
    return new Phi(mem, new java.util.ArrayList<>(Collections.nCopies(n, mem)));
  }

  private static class MirNodes implements GraphNodes<BasicBlock, MirContext> {
    final BasicBlock entry = new BasicBlock();
    final BasicBlock exit = new BasicBlock();

    @Override
    public Collection<BasicBlock> nodesOf(final MirContext mir) {
      final List<BasicBlock> blocks = new java.util.ArrayList<>(mir.blocks.size() + 2);
      blocks.add(entry);
      blocks.addAll(mir.blocks);
      blocks.add(exit);

      return blocks;
    }

    @Override
    public List<BasicBlock> successorsOf(final BasicBlock bb, final MirContext mir) {
      if (bb.equals(entry)) {
        return Arrays.asList(mir.blocks.get(0), exit);
      }
      if (bb.equals(exit)) {
        return Collections.emptyList();
      }
      final List<BasicBlock> targets = targetsOf(bb);
      if (targets.isEmpty()) {
        return Collections.singletonList(exit);
      }
      return targets;
    }
  }

  private static Map<Static, List<DefLoc>> collectAssignments(
      final Collection<BasicBlock> blocks) {
    final Map<Static, List<DefLoc>> storage = new java.util.HashMap<>();
    for (final BasicBlock bb : blocks) {
      for (final Instruction insn : bb.insns) {
        if (insn instanceof Store) {
          final Store store = (Store) insn;
          final Static lhs = getMemory(store.target);
          List<DefLoc> defs = storage.get(lhs);
          if (defs == null) {
            defs = new java.util.ArrayList<>();
            storage.put(lhs, defs);
          }
          defs.add(new DefLoc(store, bb));
        }
      }
    }
    return storage;
  }

  private static final class DefLoc {
    final Instruction insn;
    final BasicBlock bb;

    DefLoc(final Instruction insn, final BasicBlock bb) {
      this.insn = insn;
      this.bb = bb;
    }
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

  private static List<Node> topologicalOrder(final MirContext ctx) {
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
    final Instruction insn = Lists.lastOf(bb.insns);
    if (insn instanceof Branch) {
      return ((Branch) insn).successors;
    }
    return Collections.emptyList();
  }

  static class DepthFirstPath <T> {
    public interface Sibling <T> {
      Collection<T> get(T input);
    }

    public static <T> List<T> get(final T source, final Sibling<T> sibling) {
      final List<T> path = new java.util.ArrayList<T>();

      T item = source;
      for (Collection<T> succ = sibling.get(item); !succ.isEmpty(); succ = sibling.get(item)) {
        path.add(item);
        item = succ.iterator().next();
      }
      path.add(item);

      return path;
    }
  }

  static class Forward implements DepthFirstPath.Sibling<Node> {
    @Override
    public Collection<Node> get(final Node node) {
      return node.succ;
    }
  }

  static class Backward implements DepthFirstPath.Sibling<Node> {
    @Override
    public Collection<Node> get(final Node node) {
      return node.pred;
    }
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

    @Override
    public String toString() {
      return String.format("ite %s %s %s", guard, taken, other);
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
