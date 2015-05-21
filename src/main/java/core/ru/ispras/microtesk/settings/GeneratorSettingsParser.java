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

/**
 * {@link GeneratorSettingsParser} implements a parser of {@link GeneratorSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GeneratorSettingsParser extends AbstractSettingsParser {
  public GeneratorSettingsParser() {
    super(GeneratorSettings.TAG);

    // Parsers for the standard sections.
    addParser(new MemorySettingsParser());
  }

  @Override
  public AbstractSettings createSettings(final Map<String, String> attributes) {
    return new GeneratorSettings();
  }
}
