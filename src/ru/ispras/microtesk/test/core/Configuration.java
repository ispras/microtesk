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
    public Configuration()
    {
        // Available combinators
        combinators.put(ECombinator.PRODUCT.name(),     ProductCombinator.class);
        combinators.put(ECombinator.DIAGONAL.name(),    DiagonalCombinator.class);
        combinators.put(ECombinator.RANDOM.name(),      RandomCombinator.class);
        
        // Available compositors
        compositors.put(ECompositor.CATENATION.name(),  CatenationCompositor.class);
        compositors.put(ECompositor.ROTATION.name(),    RotationCompositor.class);
        compositors.put(ECompositor.CATENATION.name(),  OverlappingCompositor.class);
        compositors.put(ECompositor.NESTING.name(),     NestingCompositor.class);
        compositors.put(ECompositor.RANDOM.name(),      RandomCompositor.class);
    }
    
    /**
     * Creates an instance of the combinator with the given name.
     *
     * @return a combinator instance.
     * @param the combinator's name.
     */
    @SuppressWarnings("unchecked")
    public BaseCombinator<T> getCombinator(final String name)
    {
        return (BaseCombinator<T>)createInstance(combinators.get(name));
    }

    /**
     * Creates an instance of the combinator with the given id.
     *
     * @return a combinator instance.
     * @param id the combinator's id.
     */
    @SuppressWarnings("unchecked")
    public BaseCombinator<T> getCombinator(final ECombinator id)
    {
        return getCombinator(id.name());
    }
    
    /**
     * Creates an instance of the compositor with the given name.
     *
     * @return a compositor instance.
     * @param name the compositor's name.
     */
    @SuppressWarnings("unchecked")
    public BaseCompositor<T> getCompositor(final String name)
    {
        return (BaseCompositor<T>)createInstance(compositors.get(name));
    }

    /**
     * Creates an instance of the compositor with the given name.
     *
     * @return a compositor instance.
     * @param id the compositor's id.
     */
    @SuppressWarnings("unchecked")
    public BaseCompositor<T> getCompositor(final ECompositor id)
    {
        return getCompositor(id.name());
    }
    
    /// Creates an instance of the given type.
    private <I> I createInstance(Class<I> type)
    {
        try
            { return type.newInstance(); }
        catch(final Exception e)
            { throw new IllegalArgumentException(); }    
    }    
}
