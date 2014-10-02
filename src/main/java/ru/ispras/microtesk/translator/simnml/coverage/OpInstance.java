package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.TreeSet;

import ru.ispras.microtesk.translator.simnml.ir.primitive.*;

class OpInstance
{
    public static class Binding implements Comparable<Binding>
    {
        public final String     name;
        public final OpInstance value;

        public Binding(String name, OpInstance value)
        {
            this.name = name;
            this.value = value;
        }

        @Override
        public int compareTo(Binding o)
        {
            if (o == null)
                throw new NullPointerException();

            return name.compareTo(o.name);
        }
    }

    private final PrimitiveAND      image;
    private final List<Binding>     bindings;
    private String                  name;
    private String                  keyName;
    private Map<String, Primitive>  unbound;
    private Map<String, SSAForm>    attributes;

    public PrimitiveAND getImage()
    {
        return image;
    }

    public List<Binding> getBindings()
    {
        return Collections.unmodifiableList(bindings);
    }

    public Map<String, Primitive> getUnbound()
    {
        return Collections.unmodifiableMap(unbound);
    }

    public boolean hasBinding(String argument)
    {
        return getBinding(argument) != null;
    }

    public OpInstance getBinding(String argument)
    {
        for (Binding b : bindings)
            if (b.name.equals(argument))
                return b.value;

        return null;
    }

    public String getName()
    {
        return name;
    }

    public String getKeyName()
    {
        return keyName;
    }

    public Map<String, SSAForm> getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    private String instantiateName()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("(").append(image.getName());

        for (Map.Entry<String, Primitive> entry : image.getArguments().entrySet())
        {
            builder.append(" ");
            final OpInstance binding = getBinding(entry.getKey());
            if (binding == null)
                builder.append(entry.getValue().getName());
            else
                builder.append(binding.getName());
        }
        builder.append(")");

        return builder.toString();
    }

    private String instantiateKeyName()
    {
        for (Binding bind : bindings)
            if (bind.value.getImage().getKind() == Primitive.Kind.OP)
                return bind.value.getImage().getName();

        return image.getName();
    }

    private Map<String, SSAForm> instantiateAttributes(List<String> order)
    {
        if (order.isEmpty())
            return Collections.emptyMap();

        // builder assumes attributes required has been built already
        // so we need this kind of side effect to get construction in
        // one go
        this.attributes = new TreeMap<String, SSAForm>();

        final SSABuilder builder = new SSABuilder(this);
        for (int i = 0; i < order.size(); ++i)
        {
            final String attribute = order.get(i);
            if (order.subList(i + 1, order.size()).contains(attribute))
                // postpone attribute instantiation
                attributes.put(attribute, SSAForm.EMPTY_FORM);
            else
                attributes.put(attribute, builder.build(attribute));
        }
        return attributes;
    }

    private void finishInstantiation(List<String> order, Map<String, List<OpInstance>> instances)
    {
        this.name = instantiateName();
        this.keyName = instantiateKeyName();
        this.unbound = instantiateUnbound();
        this.attributes = instantiateAttributes(order);
    }

    private Map<String, Primitive> instantiateUnbound()
    {
        final Map<String, Primitive> map =
            new TreeMap<String, Primitive>(image.getArguments());

        for (Binding b : bindings)
        {
            map.remove(b.name);
            for (Map.Entry<String, Primitive> entry : b.value.getUnbound().entrySet())
                map.put(Naming.addNamespace(entry.getKey(), b.name), entry.getValue());
        }
        return map;
    }

    private OpInstance(PrimitiveAND image, List<Binding> bindings)
    {
        this.image = image;
        this.bindings = bindings;
        this.name = null;
    }

    private static OpInstance createInstance(PrimitiveAND image, List<String> order, Map<String, List<OpInstance>> instances)
    {
        final OpInstance op =
            new OpInstance(image, Collections.<Binding>emptyList());
        op.finishInstantiation(order, instances);
        return op;
    }

    // create OpInstance w/o bindings, optionally reserving memory
    private static OpInstance createUnbound(PrimitiveAND image, int size)
    {
        if (size <= 0)
            throw new IllegalArgumentException();

        return new OpInstance(image, new ArrayList<Binding>(size));
    }

    private static List<OpInstance> instantiateOR(Primitive p, Map<String, List<OpInstance>> instances)
    {
        final PrimitiveOR parent = (PrimitiveOR) p;
        final List<OpInstance> list = new ArrayList<OpInstance>();
        for (Primitive child : parent.getORs())
            list.addAll(instances.get(child.getName()));

        return Collections.unmodifiableList(list);
    }

    private static List<OpInstance> instantiateAND(Primitive p, Map<String, List<OpInstance>> instances)
    {
        final PrimitiveAND leaf = (PrimitiveAND) p;

        // inspect code in attributes and collect names of bindings called
        List<String> attributeOrder = new ArrayList<String>();
        List<String> callees = Collections.emptyList();
        for (Attribute attr : leaf.getAttributes().values())
        {
            if (attr.getKind() != Attribute.Kind.ACTION)
                continue;

            List<String> prereq;
            final int index = attributeOrder.indexOf(attr.getName());

            if (index == -1)
            {
                attributeOrder.add(attr.getName());
                prereq = attributeOrder.subList(0, attributeOrder.size() - 1);
            }
            else
                prereq = attributeOrder.subList(0, index);

            callees = Utility.appendList(callees, collectCallees(attr.getStatements(), prereq));
        }

        // no calls --> no code variance --> single instance
        if (callees.isEmpty())
            return Collections.singletonList(createInstance(leaf, attributeOrder, instances));

        callees = new ArrayList<String>(new TreeSet<String>(callees));

        int instanceCount = 1;
        for (String argName : callees)
        {
            final String originName = leaf.getArguments().get(argName).getName();
            instanceCount *= instances.get(originName).size();
        }

        // reserve memory for all instances and associated bindings
        final List<OpInstance> result = new ArrayList<OpInstance>(instanceCount);
        for (int i = 0; i < instanceCount; ++i)
            result.add(createUnbound(leaf, callees.size()));

        for (String argName : callees)
        {
            final String originName = leaf.getArguments().get(argName).getName();
            final List<OpInstance> variants = instances.get(originName);

            int i = 0;
            // bind each 'variant' in 'count' instances
            final int count = instanceCount / variants.size();
            for (int j = 0; j < count; ++j)
		for (OpInstance variant : variants)
                    result.get(i++).bindings.addAll(expandBinding(argName, variant));
        }
        for (OpInstance instance : result)
            instance.finishInstantiation(attributeOrder, instances);

        return Collections.unmodifiableList(result);
    }

    private static List<Binding> expandBinding(String name, OpInstance instance)
    {
        final List<Binding> list =
            new ArrayList<Binding>(instance.getBindings().size() + 1);

        list.add(new Binding(name, instance));
        for (Binding b : instance.getBindings())
            list.add(new Binding(Naming.addNamespace(b.name, name), b.value));

        return list;
    }

    public static List<OpInstance> instantiate(Primitive p, Map<String, List<OpInstance>> instances)
    {
        if (p == null)
            throw new NullPointerException();

        if (instances == null)
            throw new NullPointerException();

        if (p.isOrRule())
            return instantiateOR(p, instances);

        return instantiateAND(p, instances);
    }

    public static List<String> collectCallees(List<Statement> code, List<String> attributes)
    {
        List<String> collected = Collections.emptyList();

        for (Statement s : code)
        {
            switch (s.getKind())
            {
            case COND:
                final StatementCondition cond = (StatementCondition) s;
                for (int i = 0; i < cond.getBlockCount(); ++i)
                {
                    final List<String> list =
                        collectCallees(cond.getBlock(i).getStatements(), attributes);
                    collected = Utility.appendList(collected, list);
                }
                break;
    
            case CALL:
                final StatementAttributeCall call = (StatementAttributeCall) s;
                if (call.getCalleeName() != null)
                    collected = Utility.appendElement(collected, call.getCalleeName());
                else if (attributes != null && !attributes.contains(call.getAttributeName()))
                    attributes.add(call.getAttributeName());

                break;

            default: break; // skip everything else
            }
        }
        return collected;
    }
}
