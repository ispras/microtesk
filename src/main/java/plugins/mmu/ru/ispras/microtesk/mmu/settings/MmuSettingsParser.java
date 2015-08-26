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

package ru.ispras.microtesk.mmu.settings;

import java.util.Map;

import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.GeneratorSettingsParser;

/**
 * {@link MmuSettingsParser} implements a parser of {@link GeneratorSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuSettingsParser extends GeneratorSettingsParser {
  public MmuSettingsParser() {
    // Parsers for the custom sections.
    addParser(new BooleanValuesSettingsParser());
    addParser(new BooleanOptionSettingsParser());
    addParser(new BufferEventsSettingsParser());
  }

  @Override
  public MmuSettings createSettings(final Map<String, String> attributes) {
    return new MmuSettings();
  }
}
