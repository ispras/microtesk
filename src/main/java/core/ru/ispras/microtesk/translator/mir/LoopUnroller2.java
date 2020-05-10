package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.tools.symexec.ControlFlowInspector;
import ru.ispras.microtesk.tools.symexec.SymbolicExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LoopUnroller2 {
    public Integer unrollFactor;
    public LoopUnroller2(Integer unrollFactor) {
        this.unrollFactor = unrollFactor;
    }

    private MirContext composeMir(final String name, final SymbolicExecutor.BodyInfo info) {
        final MirContext mir = new MirContext(name, MirBuilder.VOID_TO_VOID_TYPE);

        final int nblocks = info.bbMir.size();
        final List<MirBlock> intros = new java.util.ArrayList<>(nblocks);
        final List<MirBlock> outros = new java.util.ArrayList<>(nblocks);

        int bbIndex = 0;
        for (final MirContext body : info.bbMir) {
            final var inoutMap = info.bbInOut.get(bbIndex);
            final var linkPair = wrapInline(body, mir, inoutMap.values());
            intros.add(linkPair.first);
            outros.add(linkPair.second);
        }
        final var ranges = info.bbRange;
        for (int i = 0; i < nblocks; ++i) {
            final var range = ranges.get(i);
            final int taken = getLinkIndex(range, range.nextTaken, ranges);
            final int other = getLinkIndex(range, range.nextOther, ranges);

            final var outro = outros.get(i);
            if (taken >= 0 && taken == other) {
                outro.jump(intros.get(taken).bb);
            } else if (taken >= 0) {
                final Operand guard = outro.getLocal(evalGuardId(i, info));
                outro.append(new Instruction.Branch(guard, intros.get(taken).bb, intros.get(other).bb));
            } else {
                outro.append(new Instruction.Return(null));
            }
        }
        return mir;
    }

    private int getLinkIndex(
            final ControlFlowInspector.Range src,
            final int target,
            final List<ControlFlowInspector.Range> ranges
    ) {
        final List<ControlFlowInspector.Range> candidates = selectRanges(target, ranges);

        if (candidates.size() == 1) {
            return ranges.indexOf(candidates.get(0));
        } else if (candidates.size() > 1) {
            for (final ControlFlowInspector.Range dst : candidates) {
                if (src.isEmpty() != dst.isEmpty()) {
                    return ranges.indexOf(dst);
                }
            }
            throw new IllegalStateException();
        } else {
            return -1;
        }
    }

    private List<ControlFlowInspector.Range> selectRanges(final int start, final List<ControlFlowInspector.Range> ranges) {
        int from = -1, to = -1;
        for (int i = 0; i < ranges.size(); ++i) {
            if (ranges.get(i).start == start) {
                if (from < 0) {
                    from = i;
                }
                to = i;
            }
        }
        if (from >= 0) {
            return ranges.subList(from, to + 1);
        }
        return Collections.emptyList();
    }

    private int evalGuardId(final int index, final SymbolicExecutor.BodyInfo info) {
        final Operand guard = info.bbCond.get(index);
        // TODO enfoce guards to be Local instance
        int version = Integer.valueOf(guard.toString().substring(1));
        for (int i = 0; i < index; ++i) {
            version += info.bbMir.get(i).locals.size() - 1;
        }
        return version;
    }

    private Pair<MirBlock, MirBlock> wrapInline(
            final MirContext callee,
            final MirContext mir,
            final Collection<Pair<Static, Static>> inout) {
        final MirBlock inbb = mir.newBlock();
        final int start = mir.blocks.size();
        final int end = start + callee.blocks.size();
        Pass.inlineContext(mir, callee);
        final MirBlock outbb = mir.newBlock();


        for (final var pair : inout) {
          inbb.assign(pair.first, pair.first.newVersion(0));
          outbb.assign(pair.second.newVersion(0), pair.second);
        }

        final BasicBlock entry = mir.blocks.get(start);
        inbb.jump(entry);
        for (final BasicBlock bb : mir.blocks.subList(start, end)) {
            final int index = bb.insns.size() - 1;
            final Instruction insn = bb.insns.get(index);

            if (insn instanceof Instruction.Return) {
                bb.insns.set(index, new Instruction.Branch(outbb.bb));
            }
        }
        return new Pair<>(inbb, outbb);
    }
}
