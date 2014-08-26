/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RandomValueBuilder.java, Aug 26, 2014 10:53:08 AM Andrei Tatarnikov
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

import ru.ispras.fortress.randomizer.Randomizer;

public final class RandomValueBuilder
{
    private static final class RandomValueObject implements RandomValue
    {
        private final int min;
        private final int max;

        private int cachedValue;
        private boolean hasCachedValue;

        private RandomValueObject(int min, int max)
        {
            this.min = min;
            this.max = max;
            this.cachedValue = 0;
            this.hasCachedValue = false;
        }

        @Override
        public int getMin() { return min; }

        @Override
        public int getMax() { return max; }

        @Override
        public int getValue()
        {
            cachedValue = Randomizer.get().nextIntRange(min, max); 
            hasCachedValue = true;
            return cachedValue;
        }

        public int getCachedValue()
        {
            if (!hasCachedValue)
                throw new IllegalStateException("No random value is cached!");
            
            return cachedValue;
        }
    }

    private static final class RandomValueReference implements RandomValue
    {
        private final RandomValueObject value;

        private RandomValueReference(RandomValueObject value)
        {
            this.value = value;
        }

        @Override
        public int getMin() { return value.getMin(); }

        @Override
        public int getMax() { return value.getMax(); }

        @Override
        public int getValue() { return value.getCachedValue(); }
    }

    private RandomValueObject value;
    private RandomValueReference reference;

    private final int min;
    private final int max;

    public RandomValueBuilder(int min, int max)
    {
        if (min > max)
            throw new IllegalArgumentException(
                String.format("min (%d) must be <= max (%d)!", min, max));

        this.min = min;
        this.max = max;

        this.value = null;
        this.reference = null;
    }

    public RandomValue build()
    {
        if (null == value)
        {
            value = new RandomValueObject(min, max);
            return value;
        }

        if (null == reference)
        {
            reference = new RandomValueReference(value);
        }

        return reference;
    }
}
