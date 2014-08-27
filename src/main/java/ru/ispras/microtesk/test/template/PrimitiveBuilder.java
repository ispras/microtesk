/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveBuilder.java, Aug 27, 2014 11:08:31 AM Andrei Tatarnikov
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

import java.util.Map;

import ru.ispras.microtesk.test.template.Primitive.Kind;

// TODO: base class + implementations for MODE, OP and INSTR (temporary). 
public final class PrimitiveBuilder
{
    private Kind kind;
    private String name;
    private Map<String, Argument> args;
    
    PrimitiveBuilder()
    {
        
    }

    public Primitive build()
    {
        return new Primitive(kind, name, args);
    }
}

/*


public class AddressingModeBuilder
{
    private final String name;
    private final MetaAddressingMode metaData;

    public AddressingModeBuilder(String name, MetaAddressingMode metaData)
    {
        this.name = name;
        this.metaData = metaData;
    }

    // Constant (String or Int)
    // Random from x to y
    // Unknown (No Value) - calculated as a constraint.

    ///////////////////////////////////////////////////////////////////////
    // For Array-based syntax
    
    public void addArgument(int value)
    {
        
    }
    
    public void addArgument(String value)
    {
        
    }
    
    ///////////////////////////////////////////////////////////////////////
    // For Hash-based syntax
    
    public void setArgument(String name, int value)
    {
        
    }
    
    public void setArgument(String name, String value)
    {
        
    }
    
    public AddressingMode build()
    {
        return new AddressingMode();
    }
}

*/