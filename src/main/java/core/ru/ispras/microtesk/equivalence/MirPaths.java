package ru.ispras.microtesk.equivalence;

import org.apache.commons.collections.iterators.ReverseListIterator;
import ru.ispras.microtesk.translator.mir.BasicBlock;
import ru.ispras.microtesk.translator.mir.MirContext;
import ru.ispras.microtesk.translator.mir.Pass;

import static ru.ispras.microtesk.translator.mir.Instruction.Branch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * Iterates paths starting from all `other` branches to all `target` branches
 */
public class MirPaths {
    private MirContext mir;
    private Map<BasicBlock, Status> blockStatus;
    private List<BasicBlock> lastIncludedPath;

    public MirPaths(MirContext mir) {
        this.mir = mir;
        this.blockStatus = new HashMap<>(mir.blocks.size());
        this.mir.blocks.forEach(b -> this.blockStatus.put(b, Status.UNKNOWN));
    }

    public MirContext nextPath(Boolean includeCurrentPath) {
        if (includeCurrentPath)
            includeNextBranch();
        else
            replaceLastBranch();

        return extractPath();
    }

    private MirContext extractPath() {
        if (this.mir == null)
            return null;
        MirContext result = new MirContext(this.mir.name, this.mir.getSignature());
        List<BasicBlock> currentLevel = new ArrayList<>();
        currentLevel.add(this.mir.blocks.get(0));

        while (!currentLevel.isEmpty()) {
            for (var b : currentLevel) {
                if (this.blockStatus.get(b) == Status.GOOD)
                    result.blocks.add(b);
            }
            currentLevel = currentLevel
                    .stream()
                    .flatMap(bl -> getBranch(bl).stream())
                    .flatMap(br -> br.successors.stream())
                    .collect(Collectors.toList());
        }
        return result;
    }

    private void includeNextBranch() {
        Optional<BasicBlock> nextUnknown = findFirstUnknown(this.mir.blocks.get(0));
        if (nextUnknown.isEmpty()) {
            this.mir = null;
            return;
        }
        lastIncludedPath = new ArrayList<>();
        while (nextUnknown.isPresent()) {
            BasicBlock block = nextUnknown.get();
            this.blockStatus.put(block, Status.GOOD);
            nextUnknown = getBranch(block).map(br -> br.other);
            lastIncludedPath.add(block);
        }
    }

    private Optional<BasicBlock> findFirstUnknown(BasicBlock startFrom) {
        if (this.blockStatus.get(startFrom) == Status.UNKNOWN) {
            return Optional.of(startFrom);
        } else if (this.blockStatus.get(startFrom) == Status.BAD) {
            return Optional.empty();
        }
        var branch = getBranch(startFrom);
        return branch.flatMap(b -> b.target.values().stream().findFirst())
                .or(() -> branch.map(b -> b.other))
                .flatMap(this::findFirstUnknown);
    }

    private void replaceLastBranch() {
        Iterable<BasicBlock> iterable = () -> new ReverseListIterator(lastIncludedPath);
        List<BasicBlock> toMarkAsBad = new ArrayList<>();
        for (BasicBlock i : iterable) {
            var pathsFromCurrent = getBranch(i)
                    .stream()
                    .flatMap(b -> b.successors.stream())
                    .map(succ -> this.blockStatus.get(succ))
                    .filter(p -> p == Status.GOOD || p == Status.UNKNOWN)
                    .count();

            if (pathsFromCurrent == 2)
                break;
            toMarkAsBad.add(i);
        }
        for (BasicBlock i : toMarkAsBad) {
            this.blockStatus.put(i, Status.BAD);
        }
        includeNextBranch();
    }

    private enum Status {
        GOOD,
        BAD,
        UNKNOWN
    }

    private Optional<Branch> getBranch(BasicBlock b) {
        var insn = b.insns.get(b.insns.size() - 1);

        return insn instanceof Branch ? Optional.of((Branch) insn) : Optional.empty();
    }

}
