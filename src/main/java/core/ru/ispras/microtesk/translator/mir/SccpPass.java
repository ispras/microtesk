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

public class SccpPass extends Pass {
  @Override
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    final Frame frame = EvalContext.propagatePhi(ctx, java.util.Map.of()).getFrame();
    final SparseCCP sccp = new SparseCCP(frame);

    for (final BasicBlock bb : ctx.blocks) {
      for (final Instruction insn : bb.insns) {
        insn.accept(sccp);
      }
    }
    return ctx;
  }
}
