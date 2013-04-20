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

import ru.ispras.microtesk.test.core.Sequence;

/**
 * This class implements rotation (interleaving) composition of two sequences.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RotationCompositor<T> extends BaseCompositor<T>
{
    @SuppressWarnings("unchecked")
    public Sequence<T> compose(final Sequence<T> lhs, final Sequence<T> rhs)
    {
        Sequence<T> result = new Sequence<T>();
        
        if(lhs.isEmpty()) { result.addAll(rhs); return result; }
        if(rhs.isEmpty()) { result.addAll(lhs); return result; }
        
        int[] s = new int[] { lhs.size(), rhs.size() };        
        int[] i = new int[] { 0, 0 };

        Sequence<T>[] seqs  = new Sequence[] { lhs, rhs };
          
        while(i[0] < s[0] || i[1] < s[1])
        {
            if(i[0] < s[0]) { result.add(seqs[0].get(i[0]++)); }
            if(i[1] < s[1]) { result.add(seqs[1].get(i[1]++)); }
        }
        
        return result;
    }
}