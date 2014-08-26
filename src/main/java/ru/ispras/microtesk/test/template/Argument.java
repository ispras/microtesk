/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Argument.java, May 8, 2013 11:49:25 AM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test.template;

public final class Argument
{
    public static enum Kind
    {
         /** Constant value. */
         IMM          (Integer.class),
         /** Random value from the given range. */
         IMM_RANDOM   (RandomValue.class),
         /** Reserved, to be calculated as a constraint. */
         IMM_UNKNOWN  (null), // TODO

         /** Specific mode. */
         MODE         (null), // TODO
         /** Randomly chosen mode (from possible alternatives with a suitable signature). */
         MODE_RANDOM  (null), // TODO
         /** Reserved, to be calculated as a constraint. */
         MODE_UNKNOWN (null), // TODO

         /** Specific operation. */
         OP           (null), // TODO
         /** Randomly chosen operation (from possible alternatives with a suitable signature). */
         OP_RANDOM    (null), // TODO
         /** Reserved, to be calculated as a constraint. */
         OP_UNKNOWN   (null); // TODO

         private final Class<?> valueClass;

         private Kind(Class<?> valueClass)
             { this.valueClass = valueClass; }

         private Class<?> getValueClass()
             { return valueClass; }
    }

    private final String name;
    private final Kind kind;
    private final Object value;

    Argument(String name, Kind kind, Object value)
    {
        checkNotNull(name);
        checkNotNull(kind);
        checkNotNull(value);

        checkValidClass(kind.getValueClass(), value.getClass());

        this.name = name;
        this.kind = kind;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Kind getKind()
    {
        return kind;
    }

    public Object getValue()
    {
        return value;
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }

    private static void checkValidClass(Class<?> expected, Class<?> actual)
    {
        if (!expected.isAssignableFrom(actual))
            throw new IllegalArgumentException(String.format(
                 "%s is illegal value class, %s is expected.",
                 actual.getSimpleName(), expected.getSimpleName()));
    }
}
