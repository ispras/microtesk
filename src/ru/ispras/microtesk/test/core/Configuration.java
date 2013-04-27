/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.test.core;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.test.core.combinator.*;
import ru.ispras.microtesk.test.core.compositor.*;

/**
 * This class implements the configuration of the test sequence generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Configuration<T>
{
    /// The map of available combinators.
    private Map<String, Class<? extends BaseCombinator>> combinators =
    new HashMap<String, Class<? extends BaseCombinator>>();

    /// The map of available compositors.
    private Map<String, Class<? extends BaseCompositor>> compositors =
    new HashMap<String, Class<? extends BaseCompositor>>();
    
    /**
     * Constructs a configuration object.
     */
    Configuration()
    {
        // Available combinators
        combinators.put("product",     ProductCombinator.class);
        combinators.put("diagonal",    DiagonalCombinator.class);
        combinators.put("random",      RandomCombinator.class);
        
        // Available compositors
        compositors.put("catenation",  CatenationCompositor.class);
        compositors.put("rotation",    RotationCompositor.class);
        compositors.put("overlapping", OverlappingCompositor.class);
        compositors.put("nesting",     NestingCompositor.class);
        compositors.put("random",      RandomCompositor.class);
    }
}
