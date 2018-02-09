/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.Map;

/**
 * {@link Engine} defines an interface of abstract sequence processing engines.
 * 
 * @author <a href="mailto:kotsynyak@ispras.ru">Artem Kotsynyak</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface Engine {
  String getId();

  SequenceSelector getSequenceSelector();

  void configure(Map<String, Object> attributes);

  Iterator<AbstractSequence> solve(EngineContext engineContext, AbstractSequence abstractSequence);

  void onStartProgram();

  void onEndProgram();
}
