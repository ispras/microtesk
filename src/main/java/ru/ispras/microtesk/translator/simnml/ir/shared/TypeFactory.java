/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TypeExprFactory.java, Oct 22, 2012 1:53:18 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.simnml.ir.shared;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class TypeFactory extends WalkerFactoryBase
{
    public TypeFactory(WalkerContext context)
    {
        super(context);
    }

    public Type newAlias(Where where, String name) throws SemanticException
    {
        final Type ref = getIR().getTypes().get(name); 

        if (null == ref)
            raiseError(where, String.format("Undefined type: %s.", name));

        return ref.alias(name);
    }

    public Type newInt(Where where, Expr bitSize) throws SemanticException
    {
        return Type.INT(bitSize);
    }

    public Type newCard(Where where, Expr bitSize) throws SemanticException
    {
        return Type.CARD(bitSize);
    }

    public Type newFloat(
        Where where, Expr fractionBitSize, Expr exponentBitSize)
        throws SemanticException
    {
        return Type.FLOAT(fractionBitSize, exponentBitSize);
    }
}
