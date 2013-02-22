/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprTypeCastRules.java, Jan 28, 2013 2:34:55 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.model.api.type.ETypeID;

final class JavaTypeRules
{
    public static final Class<?>       ALL_TYPES[] = { int.class, long.class, double.class, boolean.class };
    public static final Class<?>   BOOLEAN_TYPES[] = { boolean.class };
    public static final Class<?>   NUMERIC_TYPES[] = { int.class, long.class, double.class };
    public static final Class<?>   INTEGER_TYPES[] = { int.class, long.class };
    public static final Class<?>       BIT_TYPES[] = { int.class, long.class, boolean.class };
    public static final Class<?> ROTATABLE_TYPES[] = { int.class };

    public static abstract class Cast
    {
        private final Class<?> target;

        public Cast(Class<?> target)
        { 
            this.target = target;
        }

        public Class<?> getTargetType()
        {
            return target;
        }

        public abstract Object cast(Object src);
    }

    private final static Cast INT_CAST = new Cast(int.class)
    {
        @Override
        public Object cast(Object src)
        {
            if (src.getClass().equals(Integer.class))
                return src;

            return new Integer(src.toString());
        }
    };

    private final static Cast LONG_CAST = new Cast(long.class)
    {
        @Override
        public Object cast(Object src)
        {
            if (src.getClass().equals(Long.class))
                return src;

            return new Long(src.toString());
        }
    };

    private final static Cast DOUBLE_CAST = new Cast(double.class)
    {
        @Override
        public Object cast(Object src)
        {
            if (src.getClass().equals(Double.class))
                return src;

            return new Double(src.toString());
        }
    };

    private final static Cast BOOLEAN_CAST = new Cast(boolean.class)
    {
        @Override
        public Object cast(Object src)
        {
            if (src.getClass().equals(Boolean.class))
                return src;

            return new Boolean(src.toString());
        }
    };

    private static final Cast CAST_MAP[][]=
    {
        { null,          INT_CAST,     LONG_CAST,    DOUBLE_CAST,  BOOLEAN_CAST },
        { INT_CAST,      INT_CAST,     LONG_CAST,    DOUBLE_CAST,  null         },
        { LONG_CAST,     LONG_CAST,    LONG_CAST,    DOUBLE_CAST,  null         },
        { DOUBLE_CAST,   DOUBLE_CAST,  DOUBLE_CAST,  DOUBLE_CAST,  null         },
        { BOOLEAN_CAST,  null,         null,         null,         BOOLEAN_CAST }
    };

    public static Cast getCast(Class<?> left, Class<?> right)
    {
        int col = 0; // left -> col
        for (int columnIndex = 1; columnIndex < CAST_MAP[0].length; ++columnIndex)
        {
            if (CAST_MAP[0][columnIndex].getTargetType().equals(left))
            {
                col = columnIndex;
                break;
            }
        }

        if (0 == col) // left is not found
            return null;

        int row = 0; // right -> row
        for (int rowIndex = 1; rowIndex < CAST_MAP.length; ++rowIndex)
        {
            if (CAST_MAP[rowIndex][0].getTargetType().equals(right))
            {
                row = rowIndex;
                break;
            }
        }

        if (0 == row) // right is not found
            return null;

        return CAST_MAP[col][row];
    }
}

final class ModelTypeRules
{
    private static final ETypeID CAST_TYPE_MAP[][]=
    {
        { null,          ETypeID.CARD,  ETypeID.INT,  ETypeID.FLOAT,  ETypeID.BOOL },
        { ETypeID.CARD,  ETypeID.CARD,  ETypeID.INT,  null,           null         },
        { ETypeID.INT,   ETypeID.INT,   ETypeID.INT,  null,           null         },
        { ETypeID.FLOAT, null,          null,         ETypeID.FLOAT,  null         },
        { ETypeID.BOOL,  null,          null,         null,           ETypeID.BOOL },
    };

    public static ETypeID getCastType(ETypeID left, ETypeID right)
    {
        int col = 0; // left -> col
        for (int columnIndex = 1; columnIndex < CAST_TYPE_MAP[0].length; ++columnIndex)
        {
            if (CAST_TYPE_MAP[0][columnIndex] == left)
            {
                col = columnIndex;
                break;
            }
        }

        if (0 == col) // left is not found
            return null;

        int row = 0; // right -> row
        for (int rowIndex = 1; rowIndex < CAST_TYPE_MAP.length; ++rowIndex)
        {
            if (CAST_TYPE_MAP[rowIndex][0] == right)
            {
                row = rowIndex;
                break;
            }
        }

        if (0 == row) // right is not found
            return null;

        return CAST_TYPE_MAP[col][row];
    }
}
