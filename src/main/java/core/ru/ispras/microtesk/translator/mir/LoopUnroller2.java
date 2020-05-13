package ru.ispras.microtesk.translator.mir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.ispras.microtesk.tools.symexec.ControlFlowInspector.Range;
import static ru.ispras.microtesk.tools.symexec.SymbolicExecutor.BodyInfo;

public class LoopUnroller2 {
    public Integer unrollFactor;
    public LoopUnroller2(Integer unrollFactor) {
        this.unrollFactor = unrollFactor;
    }

    public void unroll(BodyInfo info) {

        for (int i = 0; i < info.bbRange.size(); i++) {
            Range range = info.bbRange.get(i);
            var otherI = getLinkIndex(range, range.nextOther, info.bbRange);
            if (otherI <= i && otherI != -1) {
                unroll(info, otherI, i + 1, range.nextOther);
                i += (i + 1 - otherI) * unrollFactor;
            }
            var takenI = getLinkIndex(range, range.nextTaken, info.bbRange);
            if (takenI <= i && takenI != -1) {
                unroll(info, takenI, i + 1, range.nextTaken);
                i += (i + 1 - takenI) * unrollFactor;
            }
        }
        for (int i = 0; i < info.bbRange.size(); i++) {
           Range r = info.bbRange.get(i);
           System.out.printf("From %d to %d taken %d other %d secondaryTaken %d secondaryOther %d\n", r.start, r.end, r.nextTaken, r.nextOther, r.secondaryTaken, r.secondaryOther);
        }
    }

    private void unroll(BodyInfo info, int from, int to, int fromInstruction) {
        System.out.printf("Unrolling from %d to %d\n", from, to);
        var copiedBBMir = new ArrayList<MirContext>();
        var copiedRanges = new ArrayList<Range>();
        for (int i = 1; i < unrollFactor; i++){
            copiedBBMir.addAll(info.bbMir.subList(from, to).stream().map(Pass::copyOf).collect(Collectors.toList()));

            List<Range> copy = info.bbRange
                    .subList(from, to)
                    .stream()
                    .map(Range::copy)
                    .collect(Collectors.toList());
            pointToLoopUnroll(copy, i+1, fromInstruction);
            copiedRanges.addAll(copy);
        }
        pointToLoopUnroll(info.bbRange.subList(from, to), 1, fromInstruction);
        var lastIteration = copiedRanges.get(copiedRanges.size() - 1);
        if (lastIteration.nextTaken == fromInstruction) {
            lastIteration.nextTaken = lastIteration.nextOther;
        } else if (lastIteration.nextOther == fromInstruction) {
            lastIteration.nextOther = lastIteration.nextTaken;
        } else {
            throw new IllegalStateException();
        }
        info.bbMir.addAll(to, copiedBBMir);
        info.bbRange.addAll(to, copiedRanges);
//        System.out.println("bbMir size after unrolling " + info.bbMir.size());
//        var loopingBB = info.bbRange.get(to - 1);
//        if (getLinkIndex(loopingBB, loopingBB.nextOther, info.bbRange) == from) {
//            loopingBB.nextOther = to + 1;
//        } else if (getLinkIndex(loopingBB, loopingBB.nextTaken, info.bbRange) == from) {
//            loopingBB.nextTaken = to + 1;
//        } else {
//            throw new IllegalStateException();
//        }
//        var endOfUnrolledPart = to + (to - from) * unrollFactor;
//        for (Range r : info.bbRange.subList(endOfUnrolledPart + 1, info.bbRange.size())) {
//            if (r.nextTaken > endOfUnrolledPart)
//                r.nextTaken += (to - from) * unrollFactor;
//            if (r.nextOther > endOfUnrolledPart)
//                r.nextOther += (to - from) * unrollFactor;
//        }
    }

    private void pointToLoopUnroll(List<Range> ranges, int unrollNum, int loopAddress) {
        var lastRange = ranges.get(ranges.size() - 1);
        if (lastRange.nextTaken == loopAddress) {
            lastRange.secondaryTaken = unrollNum;
        } else if (lastRange.nextOther == loopAddress) {
            lastRange.secondaryOther = unrollNum;
        } else {
            throw new IllegalStateException();
        }
    }

    private int getLinkIndex(final Range src, final int target, final List<Range> ranges) {
        final List<Range> candidates = selectRanges(target, ranges);

        if (candidates.size() >= 1) {
            return ranges.indexOf(candidates.get(0));
        } else {
            return -1;
        }
    }

    private List<Range> selectRanges(final int start, final List<Range> ranges) {
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
}
