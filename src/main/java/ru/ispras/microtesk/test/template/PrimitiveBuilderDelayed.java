package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;

public final class PrimitiveBuilderDelayed
{
    private static final String ERR_WRONG_USE = 
        "Illegal use: Arguments can be added using either " + 
        "addArgument or setArgument methods, but not both.";

    private final CallBuilder callBuilder;
    private final MetaModel metaModel;

    private final String name;
    private String contextName;

    private final List<Argument> argumentList;
    private final Map<String, Argument> argumentMap;

    PrimitiveBuilderDelayed(
         CallBuilder callBuilder, MetaModel metaModel, String name)
    {
        if (null == callBuilder)
            throw new NullPointerException();

        if (null == metaModel)
            throw new NullPointerException();
        
        if (null == name)
            throw new NullPointerException();

        this.callBuilder = callBuilder;
        this.metaModel = metaModel;

        this.name = name;
        this.contextName = null;

        this.argumentList = new ArrayList<Argument>();
        this.argumentMap = new LinkedHashMap<String, Argument>();
    }

    public Primitive build()
    {
        final MetaOperation metaData = metaModel.getOperation(name);
        if (null == metaData)
            throw new IllegalArgumentException("No such operation: " + name);

        final MetaShortcut metaShortcut =
           metaData.getShortcut(contextName);
        
        final PrimitiveBuilder builder;

        if (null != metaShortcut)
        {
            builder = new PrimitiveBuilder(
                callBuilder, metaShortcut.getOperation(), contextName);
        }
        else
        {
            // If there is no shortcut for the given context,
            // the operation is used as it is.
            builder = new PrimitiveBuilder(callBuilder, metaData, null);
        }
        
        return builder.build();
    }

    public void setContext(String contextName)
    {
        this.contextName = contextName;
    }
    
    private void registerArgument(Argument argument)
    {
        if (argument.hasName())
        {
            if (!argumentList.isEmpty())
                throw new IllegalStateException(ERR_WRONG_USE);

            argumentMap.put(argument.getName(), argument);
        }
        else
        {
            if (!argumentMap.isEmpty())
                throw new IllegalStateException(ERR_WRONG_USE);

            argumentList.add(argument);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Array-based syntax

    public void addArgument(int value)
    {
        registerArgument(new ArgumentInt(value));
    }

    public void addArgument(String value)
    {
        checkNotNull(value);

        registerArgument(new ArgumentStr(value));
    }

    public void addArgument(RandomValueBuilder value)
    {
        checkNotNull(value);

        registerArgument(new ArgumentRVB(value));
    }

    public void addArgument(Primitive value)
    {
        checkNotNull(value);

        registerArgument(new ArgumentPrimitive(value));
    }

    ///////////////////////////////////////////////////////////////////////////
    // For Hash-based syntax

    public void setArgument(String name, int value)
    {
        checkNotNull(name);

        registerArgument(new ArgumentInt(name, value));
    }

    public void setArgument(String name, String value)
    {
        checkNotNull(name);
        checkNotNull(value);

        registerArgument(new ArgumentStr(name, value));
    }

    public void setArgument(String name, RandomValueBuilder value)
    {
        checkNotNull(name);
        checkNotNull(value);

        registerArgument(new ArgumentRVB(name, value));
    }

    public void setArgument(String name, Primitive value)
    {
        checkNotNull(name);
        checkNotNull(value);

        registerArgument(new ArgumentPrimitive(name, value));
    }

    private static void checkNotNull(Object o)
    {
        if (o == null)
            throw new NullPointerException();
    }

    private interface Argument
    {
        boolean hasName();
        String getName();
        void addToBuilder(PrimitiveBuilder builder);
    }

    private static abstract class AbstractArgument<T> implements Argument
    {
        private final String name;
        private final T value;

        public AbstractArgument(String name, T value)
        {
            this.name = name;
            this.value = value;
        }

        public AbstractArgument(T value)
        {
            this(null, value);
        }

        @Override
        public final String getName()
        {
            return name;
        }

        @Override
        public final boolean hasName()
        {
            return null != name;
        }

        public final T getValue() 
        {
            return value;
        }
    }

    private static class ArgumentInt extends AbstractArgument<Integer>
    {
        public ArgumentInt(String name, int value)
            { super(name, value); }

        public ArgumentInt(int value)
            { super(value); }

        @Override
        public void addToBuilder(PrimitiveBuilder builder)
        {
            if (hasName())
                builder.setArgument(getName(), getValue());
            else
                builder.addArgument(getValue());
        }
    }

    private static class ArgumentStr extends AbstractArgument<String>
    {
        public ArgumentStr(String name, String value)
            { super(name, value); }

        public ArgumentStr(String value)
            { super(value); }

        @Override
        public void addToBuilder(PrimitiveBuilder builder)
        {
            if (hasName())
                builder.setArgument(getName(), getValue());
            else
                builder.addArgument(getValue());
        }
    }

    private static class ArgumentRVB extends AbstractArgument<RandomValueBuilder>
    {
        public ArgumentRVB(String name, RandomValueBuilder value)
            { super(name, value); }

        public ArgumentRVB(RandomValueBuilder value)
            { super(value); }

        @Override
        public void addToBuilder(PrimitiveBuilder builder)
        {
            if (hasName())
                builder.setArgument(getName(), getValue());
            else
                builder.addArgument(getValue());
        }
    }

    private static class ArgumentPrimitive extends AbstractArgument<Primitive>
    {
        public ArgumentPrimitive(String name, Primitive value)
            { super(name, value); }

        public ArgumentPrimitive(Primitive value)
            { super(value); }

        @Override
        public void addToBuilder(PrimitiveBuilder builder)
        {
            if (hasName())
                builder.setArgument(getName(), getValue());
            else
                builder.addArgument(getValue());
        }
    }
}
