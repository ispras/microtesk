/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaShortcut.java, Jul 7, 2014 11:00:29 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

/**
 * The MetaShortcut class describes a shortcut way to refer to
 * an operation in some specific context. A shortcut is composition
 * of operations. Shortcuts can be used when there is a unique way
 * to build a composite object. The context is the name of the 
 * operation to be parameterized with a shortcut object. 
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaShortcut
{
    private final String contextName;
    private final MetaOperation operation;
    
    /**
     * Creates a shortcut object.
     * 
     * @param contextName Context identifier.
     * @param operation Description of the shortcut operation signature. 
     */

    public MetaShortcut(String contextName, MetaOperation operation)
    {
        this.contextName = contextName;
        this.operation = operation;
    }

    /**
     * Returns the context identifier that describes the operation
     * that can be parameterized with (can refer to) the given 
     * shortcut operation. 
     * 
     * @return Name of the context in which the shortcut can be referred. 
     */

    public String getContextName()
    {
        return contextName;
    }

    /**
     * Returns a metadata object describing the signature of
     * the shortcut operation.
     * 
     * @return Metadata describing the shortcut operation.
     */

    public MetaOperation getOperation()
    {
        return operation;
    }
}
