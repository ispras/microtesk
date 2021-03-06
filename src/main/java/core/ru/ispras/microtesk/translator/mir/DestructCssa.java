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

import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.SsaStore;
import static ru.ispras.microtesk.translator.mir.Instruction.Load;
import static ru.ispras.microtesk.translator.mir.Instruction.Store;

/** Destruction of conventional SSA form.
 *  Current design allows simple phi- and subscript removal.
 */
public final class DestructCssa extends Pass {
  @Override
  public MirContext apply(final MirContext input) {
    final var mir = Pass.copyOf(input);
    for (var bb : mir.blocks) {
      for (var it = bb.insns.listIterator(); it.hasNext(); ) {
        final Instruction insn = it.next();
        if (insn instanceof Phi) {
          it.remove();
        } else if (insn instanceof SsaStore) {
          final var store = (SsaStore) insn;
          it.set(new Store(removeSubscript(store.origin.target), store.origin.source));
        } else if (insn instanceof Load) {
          final var load = (Load) insn;
          it.set(new Load(removeSubscript(load.source), load.target));
        }
      }
    }
    return mir;
  }

  private static Lvalue removeSubscript(final Lvalue lval) {
    if (lval instanceof Static) {
      return ((Static) lval).removeSubscript();
    } else if (lval instanceof Index) {
      final var index = (Index) lval;
      return new Index(removeSubscript(index.base), index.index);
    } else if (lval instanceof Field) {
      final var field = (Field) lval;
      return new Field(removeSubscript(field.base), field.name);
    }
    throw new IllegalArgumentException(
      "Unsupported L-value class " + lval.getClass().getName());
  }
}
