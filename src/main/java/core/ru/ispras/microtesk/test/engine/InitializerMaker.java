/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.test.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.TestData;

/**
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface InitializerMaker {
  void configure(Map<String, Object> attributes);

  void onStartProgram();

  void onEndProgram();

  List<AbstractCall> makeInitializer(
      final EngineContext engineContext,
      final AbstractCall abstractCall,
      final Primitive primitive,
      final Situation situation,
      final TestData testData,
      final Map<String, Argument> modes,
      final Set<AddressingModeWrapper> initializedModes) throws ConfigurationException;
}
