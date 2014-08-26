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
         IMM        (Integer.class),
         IMM_RANDOM (RandomValue.class),
         MODE       (Primitive.class),
         OP         (Primitive.class);

         private static String ILLEGAL_CLASS =
             "%s is illegal value class, %s is expected.";

         private final Class<?> vc;

         private Kind(Class<?> valueClass)
             { this.vc = valueClass; }

         private void checkClass(Class<?> c)
         {
             if (!vc.isAssignableFrom(c))
                 throw new IllegalArgumentException(String.format(
                      ILLEGAL_CLASS, c.getSimpleName(), vc.getSimpleName()));
         }
    }

    private final String name;
    private final Kind kind;
    private final Object value;

    Argument(String name, Kind kind, Object value)
    {
        if (null == name)
            throw new NullPointerException();

        if (null == kind)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        kind.checkClass(value.getClass());

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
}
