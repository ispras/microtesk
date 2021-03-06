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
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.mir.Instruction.Branch;

public abstract class Pass {
  private String comment = "";
  Map<String, MirContext> storage;

  public abstract MirContext apply(MirContext ctx);

  public Pass setComment(final String s) {
    this.comment = s;
    return this;
  }

  public String getComment() {
    return this.comment;
  }

  protected MirContext resolveCallee(final String name) {
    return storage.get(name);
  }

  static MirContext copyOf(final MirContext src) {
    return copyOf(src, src.name);
  }

  static MirContext copyOf(final MirContext src, final String name) {
    return inlineContext(new MirContext(name, src.getSignature()), src);
  }

  public static MirContext inlineContext(final MirContext dst, final MirContext src) {
    final int nshift = src.getSignature().params.size() + 1;
    final int origin = dst.locals.size() - nshift;
    dst.locals.addAll(Lists.tailOf(src.locals, nshift));

    final List<BasicBlock> body = new java.util.ArrayList<>();
    for (final BasicBlock bb : src.blocks) {
      body.add(BasicBlock.copyOf(bb));
    }
    rebaseBlocks(origin, body);
    dst.blocks.addAll(body);

    for (final BasicBlock bb : body) {
      final int index = bb.insns.size() - 1;
      final Instruction insn = bb.insns.get(index);
      if (insn instanceof Branch) {
        final Branch br = (Branch) insn;
        if (br.successors.size() == 1) {
          bb.insns.set(index, new Branch(retargetBlock(br.successors.get(0), src.blocks, body)));
        } else {
          final BasicBlock taken = retargetBlock(br.successors.get(0), src.blocks, body);
          final BasicBlock other = retargetBlock(br.successors.get(1), src.blocks, body);

          bb.insns.set(index, new Branch(br.guard, taken, other));
        }
      }
    }
    return dst;
  }

  private static void rebaseBlocks(final int value, final Collection<BasicBlock> blocks) {
    blocks.stream()
      .flatMap(bb -> bb.origins.stream())
      .forEach(org -> org.value += value);
  }

  private static BasicBlock retargetBlock(
      final BasicBlock bb,
      final List<BasicBlock> origins,
      final List<BasicBlock> storage) {
    return storage.get(origins.indexOf(bb));
  }
}
