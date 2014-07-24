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

package ru.ispras.microtesk.test.core.compositor;

import java.util.Arrays;

import ru.ispras.microtesk.test.core.iterator.IBoundedIterator;
import ru.ispras.microtesk.test.core.iterator.IIterator;
import ru.ispras.fortress.randomizer.Distribution;
import ru.ispras.fortress.randomizer.Randomizer;

/**
 * This class implements the random composition (merging) of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomCompositor<T> extends Compositor<T>
{
    /// Random distribution for choosing iterators.
    private Distribution distribution;

    @Override
    protected void onInit()
    {
        boolean bounded = true;

        int[] weights = new int[iterators.size()];

        // if all of the iterators are bounded (i.e., their sequences' sizes are known),
        // the iterator choice probability is proportional to the sequence size.
        for(int i = 0; i < iterators.size(); i++)
        {
            final IIterator<T> iterator = iterators.get(i);

            if(!(iterator instanceof IBoundedIterator))
                { bounded = false; break; }

            weights[i] = ((IBoundedIterator<T>)iterator).size();
        }

        // If there are unbounded iterators (i.e., iterators with unknown size),
        // the uniform probability distribution is used for choosing iterators.
        if(!bounded)
            { Arrays.fill(weights, 1); }

        distribution = new Distribution(weights);
    }
   
    @Override
    protected void onNext()
    {
        // Do nothing
    }

    @Override
    protected IIterator<T> choose()
    {
        while(distribution.getMaxWeight() != 0)
        {
            final int i = Randomizer.get().choose(distribution);

            if(iterators.get(i).hasValue())
                { return iterators.get(i); }

            distribution.setWeight(i, 0);
        }

        return null;
    }
}
