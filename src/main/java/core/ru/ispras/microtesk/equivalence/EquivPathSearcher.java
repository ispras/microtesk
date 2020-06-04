package ru.ispras.microtesk.equivalence;

import ru.ispras.fortress.solver.Solver;
import ru.ispras.fortress.solver.SolverResult;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.ConstraintKind;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.microtesk.translator.mir.Mir2Node;
import ru.ispras.microtesk.translator.mir.MirContext;

public class EquivPathSearcher {
    private MirContext mir;
    private Solver solver;

    public EquivPathSearcher(MirContext mir, Solver solver) {
        this.mir = mir;
        this.solver = solver;
    }
    public MirContext findEquivalentPaths(MirContext path1) {
        var paths = new MirPaths(mir);
        var pathSet = paths.nextPath(true);
        while (pathSet != null) {
            var satisfies = checkEquality(pathSet, path1);
            pathSet = paths.nextPath(satisfies);
        }
        return pathSet;
    }

    private boolean checkEquality(MirContext mir1, MirContext mir2) {
        var mir2Node = new Mir2Node();
        mir2Node.apply(mir1);
        mir2Node.apply(mir2);

        var builder = new ConstraintBuilder();

        builder.setKind(ConstraintKind.FORMULA_BASED);
        var formulas = new Formulas();
        builder.setInnerRep(formulas);

        formulas.addAll(mir2Node.getFormulae());
        return this.solver.solve(builder.build()).getStatus() == SolverResult.Status.SAT;
    }
}
