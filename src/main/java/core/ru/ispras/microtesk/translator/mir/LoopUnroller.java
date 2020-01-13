package ru.ispras.microtesk.translator.mir;

import ru.ispras.microtesk.translator.mir.Instruction.Branch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoopUnroller {
    public Integer maxUnrollFactor;
    public LoopUnroller(Integer maxUnrollFactor) {
        this.maxUnrollFactor = maxUnrollFactor;
    }

    public MirContext unroll(MirContext initialPath) {
        MirContext path = Pass.copyOf(initialPath, initialPath.name + "_unrolled");
        int jumpFrom = -1;
        int jumpTo = -1;
        while (true) {
            jumpFrom++;
            while (jumpFrom < path.blocks.size()) {
                jumpTo = findBackwardsJump(jumpFrom, path.blocks);
                jumpFrom++;
                if (jumpTo != -1) break;
            }
            jumpFrom--;
            if (jumpTo == -1) {
                System.out.println("Found no backwards jumps");
                break;
            }
            unroll(path, jumpTo, jumpFrom);
            jumpFrom += (maxUnrollFactor - 1) * (jumpFrom - jumpTo + 1);
        }
        return path;
    }

    private void unroll(MirContext ctx, int unrollFrom, int unrollTo) {
        List<BasicBlock> nUnrolls = partiallyCopy(ctx, unrollFrom, unrollTo + 1);
        for (int i = 0; i < maxUnrollFactor - 2; i++) {
            List<BasicBlock> nextUnroll = partiallyCopy(ctx, unrollFrom, unrollTo + 1);
            chain(nUnrolls, nextUnroll);
            nUnrolls.addAll(nextUnroll);
        }
        var lastPiece = genPathToLoopExit(nUnrolls, nUnrolls.size() - (unrollTo - unrollFrom) - 1, nUnrolls.size() - 1);
        chain(nUnrolls, lastPiece);
        nUnrolls.addAll(lastPiece);
        chain(ctx.blocks.subList(unrollFrom, unrollTo + 1), nUnrolls);
        ctx.blocks.addAll(unrollTo + 1, nUnrolls);
    }

    private void chain(List<BasicBlock> l1, List<BasicBlock> l2) {
        var lastBlock = l1.get(l1.size() - 1);
        var lastInsn = (Branch) lastBlock.insns.get(lastBlock.insns.size() - 1);
        var takenOptional = lastInsn.target.values().stream().findFirst().map(t -> l1.contains(t) ? l2.get(0) : t);
        var other = l1.contains(lastInsn.other) ? l2.get(0) : lastInsn.other;
        var newBranch = takenOptional.map(taken -> new Branch(lastInsn.guard, taken, other)).orElse(new Branch(other));
        lastBlock.insns.set(lastBlock.insns.size() - 1, newBranch);
    }

    List<BasicBlock> genPathToLoopExit(List<BasicBlock> blocks, int from, int to) {
        var current = blocks.get(to);
        var next = ((Branch) current.insns.get(current.insns.size() - 1)).successors;
        var result = new ArrayList<BasicBlock>();
        var loop = blocks.subList(from, to + 1);

        while (loop.containsAll(next)) {
            if (next.size() == 2) {
                throw new RuntimeException();
            }
            var newTarget = BasicBlock.copyOf(next.get(0));
            current.insns.set(current.insns.size() - 1, new Branch(newTarget));
            result.add(newTarget);
            current = newTarget;
            next = ((Branch) current.insns.get(current.insns.size() - 1)).successors;
        }
        if (next.size() == 2) {
            var nextOptional = next.stream().filter(b -> !loop.contains(b)).findFirst();
            current.insns.set(current.insns.size() - 1, new Branch(nextOptional.orElseThrow()));
        }
        return result;
    }

    private List<BasicBlock> partiallyCopy(MirContext ctx, int from, int to) {
        System.out.println(String.format("Copying from %d to %d", from, to));
        List<BasicBlock> toCopy = ctx.blocks.subList(from, to);
        List<BasicBlock> result = new java.util.ArrayList<>();
        for (final BasicBlock bb : toCopy) {
            result.add(BasicBlock.copyOf(bb));
        }

        for (final BasicBlock bb : result) {
            final int index = bb.insns.size() - 1;
            final Instruction insn = bb.insns.get(index);
            if (insn instanceof Branch) {
                final Branch br = (Branch) insn;
                List<BasicBlock> targets = new ArrayList<>();
                for (BasicBlock succ : br.successors) {
                    int idx = ctx.blocks.indexOf(succ);
                    BasicBlock target;
                    if (idx >= from && idx < to) {
                        target = result.get(idx - from);
                    } else {
                        target = ctx.blocks.get(idx);
                    }
                    targets.add(target);
                }
                if (targets.size() == 1) {
                    bb.insns.set(index, new Branch(targets.get(0)));
                } else {
                    bb.insns.set(index, new Branch(br.guard, targets.get(0), targets.get(1)));
                }
            }
        }
        return result;
    }

    private int findBackwardsJump(int blockToSearch, List<BasicBlock> path) {
        BasicBlock jumpFrom = path.get(blockToSearch);
        Instruction lastInsn = jumpFrom.insns.get(jumpFrom.insns.size() - 1);
        if (lastInsn instanceof Branch) {
            Branch br = (Branch) lastInsn;
            List<BasicBlock> jumpTargets = new ArrayList<>(br.target.values());
            jumpTargets.add(br.other);
            List<BasicBlock> beforeJump = path.subList(0, blockToSearch + 1);
            for (BasicBlock b : jumpTargets) {
                int jumpIndex = beforeJump.indexOf(b);
                if (jumpIndex != -1) {
                    if (pathExists(b, jumpFrom)) {
                        System.out.println(String.format("Found backwards jump from %d to %d", blockToSearch, jumpIndex));
                        return jumpIndex;
                    }
                }
            }
        }
        return -1;
    }

    private boolean pathExists(BasicBlock from, BasicBlock to) {
        int maxDepth = 9;
        List<BasicBlock> currentLevel = List.of(from);
        for (int i = 0; i < maxDepth; i++) {
            if (currentLevel.contains(to)) {
                return true;
            }
            currentLevel =
                    currentLevel.stream()
                            .map(b -> b.insns.get(b.insns.size() - 1))
                            .filter(lastInsn -> lastInsn instanceof Branch)
                            .flatMap(lastInsn -> ((Branch) lastInsn).successors.stream())
                            .collect(Collectors.toList());
        }
        return false;
    }

}
