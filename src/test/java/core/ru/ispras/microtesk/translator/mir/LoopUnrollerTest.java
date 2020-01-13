package ru.ispras.microtesk.translator.mir;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LoopUnrollerTest {
    @Test
    public void test() {
        var ctx = new MirContext("name", new FuncTy(Types.VOID, Collections.emptyList()));
        var b0 = ctx.newBlock();
        var b1 = ctx.newBlock();
        var b2 = ctx.newBlock();
        var b3 = ctx.newBlock();
        var b4 = ctx.newBlock();
        b0.assign(new Local(0, null), new Constant(0, 0));
        b0.jump(b1);
        b1.assign(new Local(1, null), new Constant(0, 0));
        b1.append(new Instruction.Branch(new Constant(0, 0), b2.bb, b4.bb));
        b2.assign(new Local(2, null), new Constant(0, 0));
        b2.jump(b3);
        b3.assign(new Local(3, null), new Constant(0, 0));
        b3.jump(b1);
        b4.assign(new Local(4, null), new Constant(0, 0));
        b4.bb.insns.add(new Instruction.Return(null));

        var unroller = new LoopUnroller(2);
        var result = unroller.unroll(ctx);
        var blocks = result.blocks;
        System.out.println(MirText.toString(result));
        checkAssignment(blocks.get(0), 0);
        checkAssignment(blocks.get(1), 1);
        checkAssignment(blocks.get(2), 2);
        checkAssignment(blocks.get(3), 3);
        checkAssignment(blocks.get(4), 1);
        checkAssignment(blocks.get(5), 2);
        checkAssignment(blocks.get(6), 3);
        checkAssignment(blocks.get(7), 1);
        checkAssignment(blocks.get(8), 4);
        branchesTo(blocks.get(0), blocks.get(1));
        branchesTo(blocks.get(1), blocks.get(8), blocks.get(2));
        branchesTo(blocks.get(2), blocks.get(3));
        branchesTo(blocks.get(3), blocks.get(4));
        branchesTo(blocks.get(4), blocks.get(5), blocks.get(8));
        branchesTo(blocks.get(5), blocks.get(6));
        branchesTo(blocks.get(6), blocks.get(7));
        branchesTo(blocks.get(7), blocks.get(8));
    }

    private void checkAssignment(BasicBlock b, int expectedId) {
        assertEquals(expectedId, ((Local) ((Instruction.Assignment) b.insns.get(0)).lhs).id);
    }

    private void branchesTo(BasicBlock subj, BasicBlock target) {
        var lastInsn = (Instruction.Branch) subj.insns.get(subj.insns.size() - 1);
        assertEquals(target, lastInsn.other);
        assertTrue(lastInsn.target.isEmpty());
    }

    private void branchesTo(BasicBlock subj, BasicBlock target1, BasicBlock target2) {
        var lastInsn = (Instruction.Branch) subj.insns.get(subj.insns.size() - 1);
        assertTrue(lastInsn.successors.containsAll(List.of(target1, target2)));
    }
}