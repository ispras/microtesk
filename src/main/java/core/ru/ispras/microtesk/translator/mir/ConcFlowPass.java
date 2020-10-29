/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConcFlowPass extends Pass {
  @Override
  public MirContext apply(final MirContext src) {
    final MirContext ctx = Pass.copyOf(src);
    final Map<BasicBlock, List<BasicBlock>> backrefs = new java.util.IdentityHashMap<>();

    // add jumps
    for (final BasicBlock bb : ctx.blocks) {
      final List<BasicBlock> targets = targetsOf(bb);
      if (targets.size() == 1) {
        put(backrefs, targets.get(0), bb);
      }
    }
    // remove branches
    for (final BasicBlock bb : ctx.blocks) {
      final List<BasicBlock> targets = targetsOf(bb);
      if (targets.size() > 1) {
        for (final BasicBlock next : targets) {
          backrefs.remove(next);
        }
      }
    }
    // filter non-unique targets
    final Iterator<BasicBlock> it = backrefs.keySet().iterator();
    while (it.hasNext()) {
      final BasicBlock bb = it.next();
      if (backrefs.get(bb).size() != 1) {
        it.remove();
      }
    }
    for (final BasicBlock bb : backrefs.keySet()) {
      inlineBlock(bb, backrefs);
    }
    ctx.blocks.removeAll(backrefs.keySet());

    return ctx;
  }

  private static void inlineBlock(final BasicBlock bb, final Map<BasicBlock, List<BasicBlock>> backrefs) {
    final List<BasicBlock> targets = targetsOf(bb);
    if (targets.size() == 1 && backrefs.containsKey(targets.get(0))) {
      inlineBlock(targets.get(0), backrefs);
    }
    final BasicBlock src = backrefs.get(bb).get(0);
    if (targetsOf(src).contains(bb)) {
      final int index = src.insns.size() - 1;
      src.insns.remove(index);
      for (final BasicBlock.Origin org : bb.origins) {
        org.range += index;
      }
      src.insns.addAll(bb.insns);
      src.origins.addAll(bb.origins);
    }
  }

  private static List<BasicBlock> targetsOf(final BasicBlock bb) {
    final Instruction insn = Lists.lastOf(bb.insns);
    if (insn instanceof Instruction.Branch) {
      return ((Instruction.Branch) insn).successors;
    }
    return Collections.emptyList();
  }

  private static <T> T find(final Collection<? super T> source, final Class<T> cls) {
    for (final Object o : source) {
      if (cls.isInstance(o)) {
        return cls.cast(o);
      }
    }
    return null;
  }

  private static <T, U> Collection<U> put(
      final Map<T, List<U>> map,
      final T key,
      final U value) {
    return putAll(map, key, Collections.singleton(value));
  }

  private static <T, U> Collection<U> putAll(
      final Map<T, List<U>> map,
      final T key,
      final Collection<U> values) {

    List<U> stored = map.get(key);
    if (stored == null) {
      stored = new java.util.ArrayList<U>();
      map.put(key, stored);
    }
    stored.addAll(values);
    return stored;
  }
}
