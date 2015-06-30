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
 * {@link IncludeSettingsParser} implements a parser of {@link IncludeSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IncludeSettingsParser extends AbstractSettingsParser<IncludeSettings> {
  public static final String ATTR_ITEM = "item";

  public IncludeSettingsParser() {
    super(IncludeSettings.TAG);
  }

  @Override
  protected IncludeSettings createSettings(final Map<String, String> attributes) {
    final int item = AbstractSettingsParser.getDecInteger(attributes.get(ATTR_ITEM));

    return new IncludeSettings(item);
  }
}
