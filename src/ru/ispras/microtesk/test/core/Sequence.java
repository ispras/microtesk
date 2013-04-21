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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * This class describes abstract sequences of items.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Sequence<T> extends AbstractList<T>
{
    private List<T> items = new ArrayList<T>();

    ///////////////////////////////////////////////////////////////////////////
    // Implementation of the abstract list's abstract methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public T get(int i)
    {
        return items.get(i);
    }
    
    @Override
    public int size()
    {
        return items.size();
    }
}