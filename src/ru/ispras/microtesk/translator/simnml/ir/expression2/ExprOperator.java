/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperator.java, Jan 23, 2013 6:08:04 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.model.api.type.ETypeID;

public abstract class ExprOperator
{
    private static final HashMap<Class<?>, Class<?>> typeTable =
        new HashMap<Class<?>, Class<?>>();

    private static void initTypeTable()
    {
        typeTable.put(Integer.class, int.class);
        typeTable.put(Long.class,    long.class);
        typeTable.put(Double.class,  double.class);
        typeTable.put(Boolean.class, boolean.class);

        typeTable.put(int.class,     int.class);
        typeTable.put(long.class,    long.class);
        typeTable.put(double.class,  double.class);
        typeTable.put(boolean.class, boolean.class);        
    }

    public static interface IOperator
    {
        public Class<?> getJavaType();
    }

    private final String id;
    private final int priority;

    private final EOperatorID  modelOpID;
    private final Set<ETypeID> modelTypes;
    
    private final ExprOperatorRetType retType;

    public ExprOperator(
        String id,
        int priority,
        EOperatorID modelOpID,
        ETypeID[] modelTypes,
        ExprOperatorRetType retType
        )
    {
        this.id = id;
        this.priority = priority;

        this.modelOpID = modelOpID;
        this.modelTypes = createModelTypes(modelTypes);
        
        this.retType = retType;
    }

    private static Set<ETypeID> createModelTypes(ETypeID[] types)
    {
        if ((null == types) || (0 == types.length))
            return null;

        final Set<ETypeID> result = EnumSet.noneOf(ETypeID.class);

        for (ETypeID t : types)
            result.add(t);

        return result; 
    }
    
    protected static <T extends IOperator> Map<Class<?>, T> createJavaTypeOps(T[] typeOps)
    {
        if ((null == typeOps) || (0 == typeOps.length))
            return null;

        final Map<Class<?>, T> result = new HashMap<Class<?>, T>();

        for (T op : typeOps)
            result.put(op.getJavaType(), op);

        return result;
    }

    public final String getID()
    {
        return id;
    }

    public final int getPriority()
    {
        return priority;
    }

    protected final EOperatorID getModelOpID()
    {
        return modelOpID;
    }

    protected final Set<ETypeID> getModelTypes()
    {
        return modelTypes;
    }

    protected final boolean isSupported(ETypeID modelType)
    {
        if (null == getModelTypes())
            return false;

        return getModelTypes().contains(modelType);
    }

    protected abstract Set<Class<?>> getJavaTypes();

    protected final boolean isSupported(Class<?> javaType)
    {
        if (null == getJavaTypes())
            return false;

        return getJavaTypes().contains(javaType);
    }

    public ExprOperatorRetType getRetType()
    {
        return retType;
    }
    
    protected static Class<?> getPrimitive(Class<?> c)
    {
        if (typeTable.isEmpty())
            initTypeTable();

        final Class<?> result = typeTable.get(c);

        if (null != result)
        	return result;

        assert false : "No primitive type for the specified type in the table.";
        return c;
    }
}

final class ExprOperatorRetType
{
    private final Class<?> javaType;
    private final ETypeID  modelType;

    public ExprOperatorRetType(Class<?> javaType, ETypeID modelType)
    {
        this.javaType  = javaType;
        this.modelType = modelType;
    }

    public Class<?> getJavaType()
    {
        return javaType;
    }

    public ETypeID getModelType()
    {
        return modelType;
    }
}

final class ExprOperators<T extends ExprOperator>
{
    private final Map<String, T> ops = new HashMap<String, T>();

    public void addOperator(T op)
    {
        assert !ops.containsKey(op.getID());
        ops.put(op.getID(), op);
    }

    public boolean isSupported(String opID)
    {
        return ops.containsKey(opID);
    }

    public T getOperator(String opID)
    {
        assert isSupported(opID);
        return ops.get(opID);
    }
}
