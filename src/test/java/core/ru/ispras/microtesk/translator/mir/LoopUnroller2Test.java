package ru.ispras.microtesk.translator.mir;

import org.junit.Test;
import static ru.ispras.microtesk.tools.symexec.ControlFlowInspector.Range;

import java.util.Collections;
import java.util.List;

import static ru.ispras.microtesk.tools.symexec.SymbolicExecutor.BodyInfo;

import static org.junit.Assert.*;

public class LoopUnroller2Test {
    @Test
    public void testUnrolling() {
        var body = new BodyInfo(Collections.EMPTY_LIST, null);
        var mirs = Collections.nCopies(5, new MirContext("name", new FuncTy(Types.VOID, Collections.emptyList())));
        body.bbMir.addAll(mirs);
//        body.bbRange.add(new Range(0, 4))
    }
}