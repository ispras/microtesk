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
 * {@link ExtensionsSettingsParser} implements a parser of {@link ExtensionSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ExtensionsSettingsParser extends AbstractSettingsParser<ExtensionsSettings> {
  public ExtensionsSettingsParser() {
    super(ExtensionsSettings.TAG);

    addParser(new ExtensionSettingsParser());
  }

  @Override
  public ExtensionsSettings createSettings(final Map<String, String> attributes) {
    return new ExtensionsSettings();
  }
}

