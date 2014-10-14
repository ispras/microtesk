/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

public final class InstanceSpec
{
    final PrimitiveAND              origin;
    final Map<String, ArgumentSpec> arguments;

    InstanceSpec(PrimitiveAND origin, Map<String, ArgumentSpec> arguments)
    {
        this.origin = origin;
        this.arguments = arguments;
    }

    public PrimitiveAND getOrigin()
    {
        return origin;
    }

    public Map<String, ArgumentSpec> getArguments()
    {
        return arguments;
    }
}
