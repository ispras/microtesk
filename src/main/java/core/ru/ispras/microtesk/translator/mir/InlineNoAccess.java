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

import java.util.List;

import static ru.ispras.microtesk.translator.mir.Instruction.Call;

public final class InlineNoAccess extends InlinePass {
  @Override
  protected MirContext resolveCallee(
      final MirContext mir, final Call call, final int org, final EvalContext eval) {
    if (call.callee instanceof Local) {
      final var ref = (Local) call.callee;
      final var src = eval.getLocal(ref.id + org);

      if (src instanceof Local) {
        final var local = (Local) src;
        final int index = call.method.lastIndexOf(".");
        final String name = call.method.substring(index + 1);

        if (local.id <= mir.getSignature().params.size()
            && (name.equals("write") || name.equals("read"))) {
          return null;
        }
      }
    }
    return super.resolveCallee(mir, call, org, eval);
  }
}

final class InlinePreserve extends InlinePass {
  @Override
  protected void notifyInline(
      final Call call, final BasicBlock bb, final MirContext caller, final List<BasicBlock> body) {
    /* EMPTY */
  }
}
