/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.settings;

import java.util.Map;

import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationStrategy;
import ru.ispras.microtesk.test.sequence.engine.allocator.AllocationStrategyId;

/**
 * {@link StrategySettingsParser} implements a parser of {@link StrategySettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class StrategySettingsParser extends AbstractSettingsParser<StrategySettings> {
  public static final String ATTR_NAME = "name";

  public StrategySettingsParser() {
    super(StrategySettings.TAG);
  }

  @Override
  protected StrategySettings createSettings(final Map<String, String> attributes) {
    final AllocationStrategy strategy =
        AbstractSettingsParser.getEnum(AllocationStrategyId.class, attributes.get(ATTR_NAME));

    return new StrategySettings(strategy, attributes);
  }
}
