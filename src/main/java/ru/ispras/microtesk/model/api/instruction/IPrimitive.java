/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IPrimitive.java, Nov 2, 2012 2:36:58 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.instruction;

/**
 * The IPrimitive interface is a base interface for Op (specifies an operation)
 * and Mode (specifies an addressing mode) Sim-nML primitives.
 * 
 * @author Andrei Tatarnikov
 */

public interface IPrimitive
{
    /**
     * Returns textual representation of the specified primitive.
     * 
     * @return Text value.
     */

    public String syntax();

    /**
     * Returns binary representation of the specified primitive. 
     * 
     * @return Binary text.
     */

    public String image();

    /**
     * Runs the action code associated with the primitive.
     */

    public void action(); 
}
