/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SequenceBuilder.java, Aug 30, 2014 9:10:44 PM Andrei Tatarnikov
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.test.sequence.Sequence;

final class SequenceBuilder<TCall>
{
    private final List<TCall> calls;
    private final List<TCall> initialisingCalls;

    public SequenceBuilder()
    {
        this.calls = new ArrayList<TCall>();
        this.initialisingCalls = new ArrayList<TCall>();
    }

    public void addCall(TCall call)
    {
        if (null == call)
            throw new NullPointerException();

        calls.add(call);
    }

    public void addCalls(List<TCall> calls)
    {
        if (null == calls)
            throw new NullPointerException();

        calls.addAll(calls); 
    }

    public void addInitializingCall(TCall call)
    {
        if (null == call)
            throw new NullPointerException();

        initialisingCalls.add(call);
    }

    public void addInitializingCalls(List<TCall> calls)
    {
        if (null == calls)
            throw new NullPointerException();

        initialisingCalls.addAll(calls); 
    }

    public Sequence<TCall> build()
    {
        final Sequence<TCall> result = new Sequence<TCall>();

        result.addAll(initialisingCalls);
        result.addAll(calls);

        return result;
    }
}
