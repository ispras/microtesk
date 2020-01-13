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
