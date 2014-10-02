package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;

import ru.ispras.fortress.data.types.bitvector.BitVector;

import ru.ispras.fortress.expression.*;

import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

import ru.ispras.microtesk.translator.simnml.ir.location.*;
import ru.ispras.microtesk.translator.simnml.ir.primitive.*;
import ru.ispras.microtesk.translator.simnml.ir.expression.*;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;

import static ru.ispras.microtesk.translator.simnml.coverage.Expression.*;

final class SSABuilder
{
    private final OpInstance    instance;
    private final String        tag;
    private VariableStore       store;
    private List<Node>          context;

    private final static class LValue
    {
        public final Variable   base;
        public final Node       index;
        public final Node       minorBit;
        public final Node       majorBit;

        public final DataType   baseType;
        public final DataType   sourceType;
        public final DataType   targetType;

        public LValue(Variable base, Node index, Node minorBit, Node majorBit, DataType baseType, DataType sourceType, DataType targetType)
        {
            this.base       = base;
            this.index      = index;
            this.minorBit   = minorBit;
            this.majorBit   = majorBit;
            this.baseType   = baseType;
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public boolean isArray()
        {
            return index != null;
        }

        public boolean hasBitfield()
        {
            return minorBit != null && majorBit != null;
        }

        public boolean hasStaticBitfield()
        {
            return  hasBitfield() &&
                    minorBit.getKind() == Node.Kind.VALUE &&
                    majorBit.getKind() == Node.Kind.VALUE;
        }
    }

    private void addToContext(Node node)
    {
        this.context = Utility.appendElement(context, node);
    }

    private void addToContext(List<Node> list)
    {
        this.context = Utility.appendList(context, list);
    }

    private final static class Intermediate
    {
        public final Node       ssa;
        public final Variable[] changed;

        public Intermediate(Node ssa, Variable ... changed)
        {
            this.ssa = ssa;
            this.changed = changed;
        }
    }

    private final static class VariableCmp implements Comparator<Variable>
    {
        @Override
        public int compare(Variable lhs, Variable rhs)
        {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    private NodeVariable createTemporary(DataType type)
    {
        return new NodeVariable(store.createTemporary(type));
    }

    private NodeVariable storeVariable(Variable var)
    {
        return new NodeVariable(store.storeVariable(var));
    }

    private NodeVariable updateVariable(Variable var)
    {
        return new NodeVariable(store.updateVariable(var));
    }

    /**
     * Assemble LValue object into Node instance representing rvalue.
     * Named variables are considered latest in current builder state.
     * Variables are not updated by this method.
     */
    private Node createRValue(LValue lvalue)
    {
        Node root = storeVariable(lvalue.base);

        if (lvalue.isArray())
            root = SELECT(root, lvalue.index);

        if (lvalue.hasStaticBitfield())
            return EXTRACT(
                (NodeValue) lvalue.minorBit,
                (NodeValue) lvalue.majorBit,
                root);

        if (lvalue.hasBitfield())
            return EXTRACT(
                NodeValue.newInteger(0),
                lvalue.targetType.getSize(),
                new NodeOperation(StandardOperation.BVLSHR, root, lvalue.minorBit));

        return root;
    }

    private Node[] createRValues(LValue[] lhs)
    {
        final Node[] arg = new Node[lhs.length];
        for (int i = 0; i < arg.length; ++i)
            arg[i] = createRValue(lhs[i]);

        return arg;
    }

    private LValue[] fetchConcatLValues(Location loc)
    {
        final LocationConcat conc = (LocationConcat) loc;

        final LValue[] lhs = new LValue[conc.getLocations().size()];
        for (int i = 0; i < lhs.length; ++i)
            lhs[i] = createLValue(conc.getLocations().get(i));

        return lhs;
    }

    private Intermediate assignmentToSSA(Statement stmt)
    {
        final StatementAssignment s = (StatementAssignment) stmt;

        final Node rhs = convertNode(s.getRight().getNode());
        if (s.getLeft() instanceof LocationAtom)
            return assignToAtom(s.getLeft(), rhs);

        if (s.getLeft() instanceof LocationConcat)
            return assignToConcat(s.getLeft(), rhs);

        throw new UnsupportedOperationException("Unexpected Location subtype occured");
    }

    private Intermediate assignToAtom(Location lhs, Node value)
    {
        final LValue lvalue = createLValue(lhs);

        final Node expr =
            (lvalue.isArray())
            ? EQ(updateArrayElement(lvalue), value)
            : EQ(updateScalar(lvalue), value);

        return new Intermediate(expr, lvalue.base);
    }

    private Node updateScalar(LValue lvalue)
    {
        if (!lvalue.hasBitfield())
            return updateVariable(lvalue.base);

        final NodeVariable older = storeVariable(lvalue.base);
        final NodeVariable newer = updateVariable(lvalue.base);

        if (lvalue.hasStaticBitfield())
            return updateStaticSubvector(newer, older, lvalue);

        return updateDynamicSubvector(newer, older, lvalue);
    }

    private Node updateArrayElement(LValue lvalue)
    {
        final Node olderArray = storeVariable(lvalue.base);
        final Node newerArray = updateVariable(lvalue.base);

        final NodeVariable newer = createTemporary(lvalue.sourceType);
        addToContext(EQ(newerArray, STORE(olderArray, lvalue.index, newer)));

        if (!lvalue.hasBitfield())
            return newer;

        final NodeVariable older = createTemporary(lvalue.sourceType);
        addToContext(EQ(older, SELECT(olderArray, lvalue.index)));

        if (lvalue.hasStaticBitfield())
            return updateStaticSubvector(newer, older, lvalue);

        return updateDynamicSubvector(newer, older, lvalue);
    }

    private Node updateStaticSubvector(NodeVariable newer, NodeVariable older, LValue lvalue)
    {
        final int olderHiBit = older.getData().getType().getSize() - 1;
        final int newerHiBit = newer.getData().getType().getSize() - 1;

        if (olderHiBit != newerHiBit)
            throw new IllegalArgumentException("Overlapping variables with different sizes is forbidden");

        final int hibit = olderHiBit;
        final NodeValue minor = (NodeValue) lvalue.minorBit;
        final NodeValue major = (NodeValue) lvalue.majorBit;

        addToContext(EQ(EXTRACT(0, minor, newer), EXTRACT(0, minor, older)));
        addToContext(EQ(EXTRACT(major, hibit, newer), EXTRACT(major, hibit, older)));

        return EXTRACT(minor, major, newer);
    }

    private Node updateDynamicSubvector(NodeVariable newer, NodeVariable older, LValue lvalue)
    {
        final int olderSize = older.getData().getType().getSize();
        final int newerSize = newer.getData().getType().getSize();

        if (olderSize != newerSize)
            throw new IllegalArgumentException("Overlapping variables with different sizes is forbidden");

        final int bitsize = olderSize;

        final NodeOperation shLeftAmount =
            new NodeOperation(StandardOperation.BVSUB,
                NodeValue.newBitVector(BitVector.valueOf(bitsize, bitsize)),
                lvalue.minorBit);

        addToContext(EQ(
            new NodeOperation(StandardOperation.BVLSHL, older, shLeftAmount),
            new NodeOperation(StandardOperation.BVLSHL, newer, shLeftAmount)));

        final NodeOperation shRightAmount =
            new NodeOperation(StandardOperation.BVADD,
                lvalue.majorBit,
                NodeValue.newBitVector(BitVector.valueOf(1, bitsize)));

        addToContext(EQ(
            new NodeOperation(StandardOperation.BVLSHR, older, shRightAmount),
            new NodeOperation(StandardOperation.BVLSHR, newer, shRightAmount)));

        final DataType subtype = lvalue.targetType;
        final NodeVariable subvector = createTemporary(subtype);

        addToContext(EQ(
            EXTRACT(
                NodeValue.newInteger(0),
                NodeValue.newInteger(subtype.getSize()),
                new NodeOperation(StandardOperation.BVLSHR, newer, lvalue.minorBit)),
            subvector));

        return subvector;
    }

    private Intermediate assignToConcat(Location lhs, Node value)
    {
        final LValue[] lvalues = fetchConcatLValues(lhs);
        final Node[] arg = new Node[lvalues.length];

        for (int i = 0; i < lvalues.length; ++i)
            if (lvalues[i].isArray())
                arg[i] = updateArrayElement(lvalues[i]);
            else
                arg[i] = updateScalar(lvalues[i]);

        final Variable[] changed = new Variable[lvalues.length];
        for (int i = 0; i < changed.length; ++i)
            changed[i] = lvalues[i].base;

        return new Intermediate(EQ(CONCAT(arg), value), changed);
    }

    private static Variable[] mergeIntermediateVariables(Intermediate ... in)
    {
        final TreeSet<Variable> variableSet = new TreeSet<Variable>(new VariableCmp());
        for (Intermediate inter : in)
            for (Variable v : inter.changed)
                variableSet.add(v);

        return variableSet.toArray(new Variable[variableSet.size()]);
    }

    private Intermediate conditionToSSA(Statement s)
    {
        final StatementCondition branch = (StatementCondition) s;

        final Node[] conditions = new Node[branch.getBlockCount() + 1];
        final Node[] variables = new Node[branch.getBlockCount()];

        for (int i = 0; i < variables.length; ++i)
            variables[i] = createTemporary(DataType.BOOLEAN);

        final Node[] guards = new Node[variables.length];
        for (int i = 0; i < guards.length; ++i)
            guards[i] = EQ(variables[i], FALSE);

        for (int i = 0; i < branch.getBlockCount(); ++i)
        {
            Node cond = null;
            if (branch.getBlock(i).isElseBlock())
            {
                final Node[] elseCond = new Node[i];
                for (int j = 0; j < i; ++j)
                    elseCond[j] = guards[j];
                cond = AND(elseCond);
            }
            else
                cond = convertNode(branch.getBlock(i).getCondition().getNode());

            conditions[i] = EQ(variables[i], cond);
        }

        final VariableStore snapshot = store.createSnapshot();

        final Intermediate[] blocks = new Intermediate[branch.getBlockCount()];
        final Intermediate[] blueprints = new Intermediate[branch.getBlockCount()];

        for (int i = 0; i < blocks.length; ++i)
        {
            blocks[i] = codeToSSA(branch.getBlock(i).getStatements());
            blueprints[i] = createPhiBlueprint(i, guards, blocks[i]);
        }
        final Variable[] changed = mergeIntermediateVariables(blocks);
        final Node[] fallback = new Node[changed.length];
        final String[] nameSet = new String[changed.length];

        for (int i = 0; i < fallback.length; ++i)
        {
            fallback[i] = EQ(
                updateVariable(changed[i]),
                new NodeVariable(snapshot.getVariable(changed[i].getName())));
            nameSet[i] = changed[i].getName();
        }

        final Node[] result = new Node[blueprints.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = createPhiNode(blueprints[i], nameSet, fallback);

        conditions[conditions.length - 1] = OR(result);
        return new Intermediate(AND(conditions), changed);
    }

    private Node createPhiNode(Intermediate blueprint, String[] nameSet, Node[] fallback)
    {
        final Node[] effective = Arrays.copyOf(fallback, fallback.length + 1);
        effective[effective.length - 1] = blueprint.ssa;

        for (Variable v : blueprint.changed)
        {
            final int pos = Arrays.binarySearch(nameSet, store.getBaseName(v));
            if (pos >= 0)
                effective[pos] = EQ(
                    ((NodeOperation) effective[pos]).getOperand(0),
                    new NodeVariable(v));
        }
        return AND(effective);
    }

    private Intermediate createPhiBlueprint(int index, Node[] guards, Intermediate inter)
    {
        final Node[] effGuards = Arrays.copyOf(guards, guards.length + 1);
        effGuards[index] = EQ(((NodeOperation) effGuards[index]).getOperand(0), TRUE);
        effGuards[effGuards.length - 1] = inter.ssa;

        final Variable[] slice = new Variable[inter.changed.length];
        for (int i = 0; i < slice.length; ++i)
            slice[i] = store.storeVariable(inter.changed[i]);

        return new Intermediate(AND(effGuards), slice);
    }

    private Intermediate callToSSA(Statement s)
    {
        final StatementAttributeCall call = (StatementAttributeCall) s;

        final OpInstance callee =
            (call.getCalleeName() != null)
            ? instance.getBinding(call.getCalleeName())
            : instance;

        final SSAForm stored = callee.getAttributes().get(call.getAttributeName());
        final SSAForm actual = stored.rebase(store.getVersions());

        final TreeSet<String> nameSet =
            new TreeSet<String>(actual.getStore().getVersions().keySet());
        nameSet.remove(actual.getStore().TEMPORARY_NAME);

        final List<Variable> tmp = new ArrayList<Variable>(nameSet.size());
        for (String name : nameSet)
            tmp.add(new Variable(name, actual.getStore().getVariable(name).getData()));

        return new Intermediate(actual.getExpression(), tmp.toArray(new Variable[tmp.size()]));
    }

    private Intermediate codeToSSA(List<Statement> code)
    {
        if (code.isEmpty())
            return new Intermediate(Expression.TRUE);

        int index = 0;
        final List<Intermediate> inter = new ArrayList<Intermediate>();
        for (Statement s : code)
        {
            switch (s.getKind())
            {
                case ASSIGN:    inter.add(assignmentToSSA(s)); break;
                case COND:      inter.add(conditionToSSA(s)); break;
                case CALL:      inter.add(callToSSA(s)); break;
                case STATUS:    break;

                default: throw new UnsupportedOperationException(s.getKind().toString());
            }
            ++index;
        }

        final Node[] parts = new Node[inter.size()];
        for (int i = 0; i < inter.size(); ++i)
            parts[i] = inter.get(i).ssa;

        final Intermediate[] tmp = inter.toArray(new Intermediate[inter.size()]);
        return new Intermediate(
            AND(parts),
            mergeIntermediateVariables(tmp));
    }

    private LValue createLValue(Location loc)
    {
        final LocationAtom atom = (LocationAtom) loc;

        String name = atom.getName();
        if (atom.getSource().getSymbolKind() == ESymbolKind.ARGUMENT)
            name = tag + "." + atom.getName();

        final DataType sourceType = Converter.getDataTypeForModel(atom.getSource().getType());
        final DataType targetType = Converter.getDataTypeForModel(atom.getType());

        Node index;
        DataType baseType;

        if (atom.getIndex() != null)
        {
            // Array type with Integer indices represented with BitVector 32
            index = convertNode(atom.getIndex().getNode());
            baseType = DataType.MAP(DataType.BIT_VECTOR(32), sourceType);
        }
        else
        {
            index = null;
            baseType = sourceType;
        }

        final Variable base = new Variable(name, baseType);
        if (atom.getBitfield() != null)
        {
            final Node minor = convertNode(atom.getBitfield().getFrom().getNode());
            final Node major = convertNode(atom.getBitfield().getTo().getNode());
            return new LValue(base, index, minor, major, baseType, sourceType, targetType);
        }

        return new LValue(base, index, null, null, baseType, sourceType, targetType);
    }

    /**
     * Convert given expression accordingly to current builder state.
     * Replaces named variables with versioned equivalents. Current builder
     * state versions are used. Context and variables are not updated.
     *
     * @param expression Expression to be converted.
     */
    private Node convertNode(Node expression)
    {
        final TransformerRule rule = new TransformerRule() {
            @Override
            public boolean isApplicable(Node in) {
                return in.getKind() == Node.Kind.VARIABLE
                    && locationFromNodeVariable((NodeVariable) in) != null;
            }

            @Override
            public Node apply(Node in)
            {
                final Location loc = locationFromNodeVariable(in);
                if (loc instanceof LocationAtom)
                    return createRValue(createLValue(loc));
                else if (loc instanceof LocationConcat)
                    return CONCAT(createRValues(fetchConcatLValues(loc)));
                else
                    throw new UnsupportedOperationException();
            }
        };

        final NodeTransformer transformer = new NodeTransformer();
        transformer.addRule(Node.Kind.VARIABLE, rule);
        transformer.walk(expression);

        return transformer.getResult().iterator().next();
    }

    private Node adoptNode(Node expression)
    {
        final TransformerRule rule = new TransformerRule() {
            @Override
            public boolean isApplicable(Node in) {
                return in.getKind() == Node.Kind.VARIABLE;
            }

            @Override
            public Node apply(Node in)
            {
                final NodeVariable node = (NodeVariable) in;
                final Location loc = locationFromNodeVariable(in);
                if (loc instanceof LocationAtom)
                    return createRValue(createLValue(loc));
                else if (loc instanceof LocationConcat)
                    return CONCAT(createRValues(fetchConcatLValues(loc)));
                else
                    throw new UnsupportedOperationException();
            }
        };

        final NodeTransformer transformer = new NodeTransformer();
        transformer.addRule(Node.Kind.VARIABLE, rule);
        transformer.walk(expression);

        return transformer.getResult().iterator().next();
    }

    /**
     * Extract Location user-data from Node instance.
     *
     * @return Location object if correct instance is attached to node,
     * null otherwise.
     */
    private static Location locationFromNodeVariable(Node node)
    {
        if (node.getUserData() instanceof NodeInfo)
        {
            final NodeInfo info = (NodeInfo) node.getUserData();
            if (info.getSource() instanceof Location)
                return (Location) info.getSource();
        }
        return null;
    }

    public SSAForm build(String attribute)
    {
        if (attribute == null)
            throw new NullPointerException();

        this.store = new VariableStore();
        this.context = Collections.emptyList();

        final Intermediate intermediate = codeToSSA(
            instance.getImage().getAttributes().get(attribute).getStatements());

        if (context.isEmpty())
            return new SSAForm(intermediate.ssa, store);

        final Node[] nodes = context.toArray(new Node[context.size() + 1]);
        nodes[nodes.length - 1] = intermediate.ssa;
        return new SSAForm(AND(nodes), store);
    }

    public static SSAForm fromMode(Primitive in)
    {
        if (in == null)
            throw new NullPointerException();

        final PrimitiveAND mode = (PrimitiveAND) in;
        if (mode.getReturnExpr() == null)
            return SSAForm.EMPTY_FORM;

        final SSABuilder builder = new SSABuilder(mode.getName());

        final Expr expr = mode.getReturnExpr();
        final Data data = Converter.toFortressData(expr.getValueInfo());

        final Node variable =
            builder.storeVariable(new Variable(mode.getName(), data));

        return new SSAForm(
            EQ(variable, builder.convertNode(expr.getNode())),
            builder.store);
    }

    private SSABuilder(String tag)
    {
        this.instance = null;
        this.tag = tag;
        this.store = new VariableStore();
        this.context = Collections.emptyList();
    }

    public SSABuilder(OpInstance instance)
    {
        if (instance == null)
            throw new NullPointerException();

        this.instance = instance;
        this.tag = instance.getImage().getName();
        this.context = Collections.emptyList();
    }
}
