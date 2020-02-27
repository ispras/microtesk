package ru.ispras.microtesk.translator.mir;

import ru.ispras.castle.util.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.ispras.microtesk.translator.mir.Instruction.Call;

public class MirPassDriver {
  private static final Pass NO_PASS = new Pass() {
    @Override public MirContext apply(MirContext mir) {
      throw new UnsupportedOperationException();
    }
  }.setComment("input");

  private final List<Pass> passList;
  private Map<String, MirContext> storage;

  public MirPassDriver(final Pass... passes) {
    this(Arrays.asList(passes));
  }

  public MirPassDriver(final List<Pass> passList) {
    this.passList = new java.util.ArrayList<>(passList);
    setStorage(new java.util.HashMap<String, MirContext>());
  }

  public static MirPassDriver newDefault() {
    return new MirPassDriver(
      new InlinePass().setComment("inline calls"),
      new ForwardPass().setComment("propagate"),
      new ConcFlowPass().setComment("inline blocks")
    );
  }

  public static MirPassDriver newOptimizing() {
    return MirPassDriver.newDefault().addAll(ssaOptimizeSequence());
  }

  public static List<Pass> ssaOptimizeSequence() {
    return Arrays.asList(
      new GlobalNumbering().setComment("build SSA"),
      new ForwardPass().setComment("SSA forward"),
      new SccpPass().setComment("Nested SCCP"),
      new ForwardPass().setComment("SCCP forward"),
      new ConcFlowPass().setComment("cherry"));
  }

  public MirPassDriver setStorage(final Map<String, MirContext> storage) {
    this.storage = storage;
    for (final Pass pass : passList) {
      pass.storage = storage;
    }
    return this;
  }

  public MirContext apply(final MirContext source) {
    MirContext ctx = source;
    debugReport(NO_PASS, source);
    for (final Pass pass : getPasses()) {
      final int nlocals = ctx.locals.size();
      ctx = pass.apply(ctx);
      debugReport(pass, ctx);
    }
    return ctx;
  }

  private static void debugReport(final Pass pass, final MirContext mir) {
    if (Logger.isDebug()) {
      Logger.debug("MIRPASS: %s", pass.getComment());
      Logger.debug(MirText.toString(mir));
    }
  }

  public Map<String, MirContext> run(final Map<String, MirContext> source) {
    Logger.message("  list dependencies...");
    final List<String> ordered = dependencyOrder(source);

    final int threshold = Math.min(500, Math.max(ordered.size() / 5 - 1, 1));

    int n = 0;
    for (final String name : ordered) {
      final MirContext ctx = apply(source.get(name));
      storage.put(name, ctx);
      if (++n % threshold == 0) {
        Logger.message("  %d/%d...", n, ordered.size());
      }
    }
    return Collections.unmodifiableMap(this.storage);
  }

  public MirPassDriver add(final Pass pass) {
    passList.add(pass);
    return this;
  }

  public MirPassDriver addAll(final Collection<Pass> passes) {
    passList.addAll(passes);
    return this;
  }

  public List<Pass> getPasses() {
    return passList;
  }

  private static List<String> dependencyOrder(final Map<String, MirContext> source) {
    final Map<String, List<String>> deps = new java.util.HashMap<>();
    for (final MirContext ctx : source.values()) {
      deps.put(ctx.name, listDeps(ctx, source));
    }
    final List<String> ordered = new java.util.ArrayList<>();
    while (!deps.isEmpty()) {
      orderDeps(deps.keySet().iterator().next(), ordered, deps);
    }
    return ordered;
  }

  private static void orderDeps(
      final String name,
      final List<String> ordered,
      final Map<String, List<String>> deps) {
    if (deps.containsKey(name)) {
      for (final String dep : deps.get(name)) {
        orderDeps(dep, ordered, deps);
      }
      ordered.add(name);
      deps.remove(name);
    }
  }

  private static List<String> listDeps(final MirContext ctx, final Map<String, MirContext> source) {
    final Set<String> deps = new java.util.HashSet<>();
    for (final BasicBlock bb : ctx.blocks) {
      for (final Instruction insn : bb.insns) {
        if (insn instanceof Call) {
          final Call call = (Call) insn;
          final String dep = InlinePass.resolveCalleeName(call);
          if (source.containsKey(dep)) {
            deps.add(dep);
          }
        }
      }
    }
    if (deps.isEmpty()) {
      return Collections.emptyList();
    }
    return new java.util.ArrayList<>(deps);
  }
}
