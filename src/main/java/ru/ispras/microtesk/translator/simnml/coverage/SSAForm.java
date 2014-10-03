package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import ru.ispras.fortress.data.Variable;

import ru.ispras.fortress.transformer.NodeTransformer;
import ru.ispras.fortress.transformer.TransformerRule;

final class SSAForm
{
    public static final SSAForm EMPTY_FORM = new SSAForm();

    private Node            expression;
    private VariableStore   store;
    private Map<String, Integer> versions;

    public static SSAForm compose(SSAForm lhs, SSAForm rhs)
    {
        final SSAForm out = rhs.rebase(lhs.versions);
        for (Map.Entry<String, Integer> entry : lhs.versions.entrySet())
            if (rhs.versions.get(entry.getKey()) == null)
                out.versions.put(entry.getKey(), entry.getValue());

        out.expression = new NodeOperation(StandardOperation.AND, lhs.expression, out.expression);
        return out;
    }

    public SSAForm rebase(final Map<String, Integer> base)
    {
        final SSAForm out = new SSAForm();

        final TransformerRule rule = new TransformerRule() {
            @Override
            public boolean isApplicable(Node node) {
                if (node.getKind() != Node.Kind.VARIABLE)
                    return false;
                final String name = ((NodeVariable) node).getName();
                return base.get(name.substring(0, name.indexOf(Naming.VERSION_DELIMITER))) != null;
            }

            @Override
            public Node apply(Node node) {
                final NodeVariable var = (NodeVariable) node;
                final int delim = var.getName().indexOf(Naming.VERSION_DELIMITER);
                final String basename = var.getName().substring(0, delim);
                final int numericId = base.get(basename) + Integer.parseInt(var.getName().substring(delim + 1));

                return new NodeVariable(new Variable(basename + Naming.VERSION_DELIMITER + numericId, var.getData()));
            }
        };

        final NodeTransformer transformer = new NodeTransformer();
        transformer.addRule(Node.Kind.VARIABLE, rule);
        transformer.walk(expression);

        out.expression = transformer.getResult().iterator().next();
        out.versions = new HashMap<String, Integer>(versions); // TODO fill, not recreate

        for (Map.Entry<String, Integer> entry : versions.entrySet())
        {
            Integer baseValue = base.get(entry.getKey());
            if (baseValue != null)
                out.versions.put(entry.getKey(), entry.getValue() + baseValue);
        }
        return out;
    }

    public SSAForm retarget(final String source, final String target)
    {
        final VariableStore targetStore = store.retarget(source, target);

        final TransformerRule rule = new TransformerRule() {
            @Override
            public boolean isApplicable(Node node) {
                return node.getKind() == Node.Kind.VARIABLE;
            }

            @Override
            public Node apply(Node node) {
                final NodeVariable var = (NodeVariable) node;
                final String name =
                    (Naming.isInNamespace(var.getName(), source))
                    ? Naming.changeNamespace(var.getName(), source, target)
                    : var.getName();

                return new NodeVariable(targetStore.getVariables().get(name));
            }
        };

        final NodeTransformer transformer = new NodeTransformer();
        transformer.addRule(Node.Kind.VARIABLE, rule);
        transformer.walk(expression);

        return new SSAForm(transformer.getResult().iterator().next(), targetStore);
    }

    private SSAForm()
    {
        expression = Expression.TRUE;
        store = VariableStore.EMPTY_STORE;
        versions = Collections.emptyMap();
    }

    SSAForm(Node expression, VariableStore store)
    {
        if (expression == null)
            throw new NullPointerException();

        if (store == null)
            throw new NullPointerException();

        this.expression = expression;
        this.store = store;
        this.versions = Collections.emptyMap();
    }

    Node getExpression()
    {
        return expression;
    }

    VariableStore getStore()
    {
        return store;
    }
}
