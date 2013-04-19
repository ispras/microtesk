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

package ru.ispras.microtesk.test.core.random;

/**
 * Modified additive lagged Fibonacci random number generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ModifiedLaggedFibonacci implements IRandomGenerator
{
    private LaggedFibonacci x;
    private LaggedFibonacci y;

    public ModifiedLaggedFibonacci()
    {
        this(0);
    }
    
    public ModifiedLaggedFibonacci(int s)
    {
        x = new LaggedFibonacci(s);
        y = new LaggedFibonacci(s + 1);
    }
    
    public void seed(int s)
    {
        x.seed(s);
        y.seed(s + 1);
    }

    public int next()
    {
        // Avoid bit correlations.
        return (x.next() & ~1) ^ (y.next() >> 1);
    }
}
