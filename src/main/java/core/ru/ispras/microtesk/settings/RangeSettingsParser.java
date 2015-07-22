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
 * {@link RangeSettingsParser} implements a parser of {@link RangeSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RangeSettingsParser extends AbstractSettingsParser<RangeSettings> {
  public static final String ATTR_MIN = "min";
  public static final String ATTR_MAX = "max";

  public RangeSettingsParser() {
    super(RangeSettings.TAG);

    addParser(new IncludeIntSettingsParser());
    addParser(new ExcludeIntSettingsParser());
  }

  @Override
  public RangeSettings createSettings(final Map<String, String> attributes) {
    final int min = AbstractSettingsParser.getDecInteger(attributes.get(ATTR_MIN));
    final int max = AbstractSettingsParser.getDecInteger(attributes.get(ATTR_MAX));

    return new RangeSettings(min, max);
  }
}
