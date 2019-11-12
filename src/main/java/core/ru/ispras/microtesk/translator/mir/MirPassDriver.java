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
  private final List<Pass> passList;
  private Map<String, MirContext> storage;

  public MirPassDriver(final Pass... passes) {
    this.passList = new java.util.ArrayList<>(Arrays.asList(passes));
    setStorage(new java.util.HashMap<String, MirContext>());
  }

  public static MirPassDriver newDefault() {
    return new MirPassDriver(
      new InlinePass().setComment("inline calls"),
      new ForwardPass().setComment("propagate"),
      new ConcFlowPass().setComment("inline blocks")
    );
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
    Logger.debug("COMPILE");
    Logger.debug(new MirText(source).toString());
    for (final Pass pass : getPasses()) {
      final int nlocals = ctx.locals.size();
      Logger.debug(pass.getComment());
      ctx = pass.apply(ctx);
      Logger.debug(new MirText(ctx).toString());
      pass.result.put(ctx.name, ctx);
    }
    return ctx;
  }

  public Map<String, MirContext> run(final Map<String, MirContext> source) {
    final List<String> ordered = dependencyOrder(source);
    for (final String name : ordered) {
      final MirContext ctx = apply(source.get(name));
      storage.put(name, ctx);
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

  public Map<String, MirContext> get(int index) {
    return getPasses().get(index).result;
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
