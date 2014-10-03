/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * DataEngine.java, Oct 3, 2014 3:18:19 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.solver.Environment;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;

public class DataEngine
{
    public DataEngine()
    {
        final String home = 
            System.getenv().get("MICROTESK_HOME") + "/tools/z3";

        if (Environment.isUnix())
        {
            Environment.setSolverPath(home + "/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath(home + "/windows/z3.exe");
        }
        else if(Environment.isOSX())
        {
            Environment.setSolverPath(home + "/osx/z3");
        }
        else
        {
            throw new IllegalStateException(String.format(
                "Failed to initialize the solver engine. " + 
                "Unsupported platform: %s", System.getProperty("os.name")));
        }
    }

    public void generateData(Situation situation, Primitive primitive)
    {
        System.out.printf("Solving situation %s for %s...%n",
            situation, primitive.getSignature());
    }
}
    /*

final TestSituation testSituation =
    testKnowledge.getSituation(p.getSituation(), p);

if (null == testSituation)
{
    System.out.printf(
        "Situation %s is not found.%n", situation.getName());
    return;
}

if (!testSituation.isApplicable(p))
{
    System.out.printf(
        "Situation %s is not applicable to %s %s.%n",
        situation.getName(), p.getKind().getText(), p.getName());
    return;
}

/*
for (Argument argument : p.getArguments().values())
{
    // TODO:
    // situation.setOutput(argument.getName());
}
*/
/*
final TestResult testResult = testSituation.solve();
if (TestResult.Status.OK == testResult.getStatus())
{
    // TODO
}

    }
}


/*

for (Argument argument : rootOperation.getArguments().values())
{
    System.out.print(" ");
    System.out.print(argument.isImmediate() ?
       AddressingModeImm.PARAM_NAME :
       ((Primitive) argument.getValue()).getName()); 
}

System.out.println(")");
*/

/*
final ISituation situation =
    instruction.createSituation(situationName);

// This is needed for situations like random that do not have a signature 
// and generate values for any parameters the client code might request.
// Other situations may ignore these calls.

for (Argument argument : rootOperation.getArguments().values())
    situation.setOutput(argument.getName());

Map<String, Data> output = null;

try 
{
    output = situation.solve();
}
catch (ConfigurationException e)
{
    System.out.printf("Warning! Failed to generate test data for the %s situation.\nReason: %s.\n",
        situationName, e.getMessage());

    return;
}

for (Map.Entry<String, Data> entry : output.entrySet())
{
    final Argument argument = rootOperation.getArguments().get(entry.getKey());

    if (null == argument)
    {
        System.out.printf("Argument %s is not defined for instruction %s.%n",
           entry.getKey(), rootOperation.getName());
        continue;
    }

    insertInitializingCalls(argument, entry.getValue());
}
*/

/*
private void insertInitializingCalls(Argument argument, Data value) throws ConfigurationException
{
    final String argumentTypeName = argument.isImmediate() ?
        AddressingModeImm.NAME : ((Primitive) argument.getValue()).getName();
    
    System.out.printf(
        "Initializer: argument: %7s, mode: %10s, value: %s (%s) %n",
        argument.getName(),
        argumentTypeName,
        Integer.toHexString(value.getRawData().intValue()),
        value.getRawData().toBinString()
    );

    for(IInitializerGenerator ig : model.getInitializers())
    {
        if (ig.isCompatible(argument))
        {
            final List<ConcreteCall> calls = ig.createInitializingCode(argument, value);
            sequenceBuilder.addInitializingCalls(calls);
            return;
        }
    }

    System.out.println(
        String.format(
            "Error! Failed to find an initializer generator for argument %s (addressing mode: %s).",
             argument.getName(),
             argumentTypeName
        )
    );
}
*/
