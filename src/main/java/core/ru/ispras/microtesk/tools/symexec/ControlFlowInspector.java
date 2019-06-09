package ru.ispras.microtesk.tools.symexec;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import ru.ispras.castle.util.Logger;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.translator.mir.Constant;
import ru.ispras.microtesk.translator.mir.Operand;

import static ru.ispras.microtesk.tools.symexec.SymbolicExecutor.BodyInfo;

public class ControlFlowInspector {
  private final Model model;
  private final BodyInfo info;
  private final List<Integer> addr;
  private final List<IsaPrimitive> body;
  private final List<Range> ranges = new java.util.ArrayList<>();
  private final Queue<Integer> queue = new java.util.ArrayDeque<>();

  public ControlFlowInspector(final Model model, final BodyInfo info) {
    this.model = model;
    this.info = info;
    this.addr = new java.util.ArrayList<>(info.body.size());
    this.body = info.body;

    int offset = 0;
    for (final IsaPrimitive insn : body) {
      addr.add(offset);
      offset += sizeOf(insn);
    }
  }

  private int sizeOf(final IsaPrimitive insn) {
    return insn.image(model.getTempVars()).length() / 8;
  }

  public List<Range> inspect() {
    queue.add(0);
    final BitSet observed = new BitSet(body.size());

    while (!queue.isEmpty()) {
      final int start = queue.remove();
      if (observed.get(start)) {
        final Range range = rangeOf(start);
        if (range != null && range.start != start) {
          ranges.add(range.split(start));
        }
      } else {
        int i = start;
        while (i < body.size() && !observed.get(i)) {
          final IsaPrimitive insn = body.get(i);
          if (isBranch(insn)) {
            final int target =
              addr.indexOf(addr.get(i) + getBranchOffset(insn));
            // only local jumps considered for cfg
            if (target >= 0 && target < body.size()) {
              final Range r = new Range(start, i + 1);
              r.nextTaken = target;
              r.nextOther = (isConditional(insn)) ? i + 1 : target;
              ranges.add(r);

              queue.add(target);
              queue.add(i + 1);
              break;
            }
          }
          observed.set(i++);
        }
        if (i >= body.size() || observed.get(i)) {
          ranges.add(new Range(start, i));
        }
      }
    }
    Collections.sort(ranges);
    return Collections.unmodifiableList(ranges);
  }

  private Range rangeOf(final int index) {
    for (final Range range : ranges) {
      if (range.contains(index)) {
        return range;
      }
    }
    return null;
  }

  public boolean isBranch(final IsaPrimitive insn) {
    final int offsetNext = sizeOf(insn);
    return getBranchOffset(insn) != offsetNext;
  }

  private int getBranchOffset(final IsaPrimitive insn) {
    final int index = body.indexOf(insn);
    final int offsetNext = sizeOf(insn);

    for (final Operand opnd : info.offsets.get(index)) {
      if (opnd instanceof Constant) {
        final int offset = ((Constant) opnd).getValue().intValue();
        if (offset != offsetNext) {
          return offset;
        }
      }
    }
    return offsetNext;
  }

  public boolean isConditional(final IsaPrimitive insn) {
    final Constant bitOne = new Constant(1, 1);
    final int index = body.indexOf(insn);

    return !info.branchCond.get(index).equals(bitOne);
  }

  public final static class Range implements Comparable<Range> {
    public int start;
    public int end;
    public int nextTaken;
    public int nextOther;

    public Range(final int start, final int end) {
      this.start = start;
      this.end = end;
      this.nextTaken = end;
      this.nextOther = end;
    }

    public int compareTo(final Range that) {
      return this.start - that.start;
    }

    public boolean contains(final int index) {
      return index >= start && index < end;
    }

    public Range split(final int index) {
      final Range tail = new Range(index, this.end);
      tail.nextTaken = this.nextTaken;
      tail.nextOther = this.nextOther;

      this.end = index;
      this.nextTaken = index;
      this.nextOther = index;

      return tail;
    }
  }
}
