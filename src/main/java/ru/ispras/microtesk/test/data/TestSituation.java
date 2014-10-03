/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, Sep 29, 2014 6:09:12 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.data;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.test.template.Primitive;

/*
class Argument
{
    public static enum Kind
    {
        IMM, // immediate
        MEM, // MEM, REG (, VAR)
        MODE // MODE
    }
}
*/

// - IMM
// - MEM, REG, VAR
// - ADDRESSING MODE

// - INPUTS
// - OUTPUTS

public final class TestSituation
{
    public static final class Variable
    {
        
    }

    private final String name;

    // Variables to be used in the constraint as input/output 
    //private final List<Variable> variables = new ArrayList<Variable>();

    // Variables (members of the constraint)

    public TestSituation(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public boolean isApplicable(Primitive p)
    {
        return false;
    }

    // - Get signature
    // - Check signature
    // - Setting input data

    public void setVariableValue(Data value)
    {
        // TODO: ???
    }

    public TestResult solve() 
    {
        // TODO
        return new TestResult(TestResult.Status.OK);
    }
}
