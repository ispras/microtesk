package ru.ispras.microtesk.translator.mir;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MirPassDriver {
  private final List<Pass> passList;
  private Map<String, MirContext> storage = new java.util.HashMap<>();

  public MirPassDriver(final Pass... passes) {
    this.passList = Arrays.asList(passes);
    for (final Pass pass : passList) {
      pass.storage = this.storage;
    }
  }

  public static MirPassDriver newDefault() {
    return new MirPassDriver(
      new ForwardPass().setComment("propagate"),
      new InlinePass().setComment("inline calls"),
      new ForwardPass().setComment("propagate2"),
      new ConcFlowPass().setComment("inline blocks")
    );
  }

  public Map<String, MirContext> run(final Map<String, MirContext> source) {
    final List<String> ordered = dependencyOrder(source);
    for (final String name : ordered) {
      MirContext ctx = source.get(name);
      for (final Pass pass : passList) {
        ctx = pass.apply(ctx);
        pass.result.put(name, ctx);
      }
      storage.put(name, ctx);
    }
    return Collections.unmodifiableMap(this.storage);
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
