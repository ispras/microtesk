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

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link RegionSettingsParser} implements a parser of {@link RegionSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class RegionSettingsParser extends AbstractSettingsParser<RegionSettings> {
  public static final String ATTR_NAME = "name";
  public static final String ATTR_TYPE = "type";
  public static final String ATTR_START = "start";
  public static final String ATTR_END = "end";
  public static final String ATTR_MODE = "mode";

  public RegionSettingsParser() {
    super(RegionSettings.TAG);

    addParser(new AccessSettingsParser());
  }

  @Override
  public RegionSettings createSettings(final Map<String, String> attributes) {
    final String name = AbstractSettingsParser.getString(attributes.get(ATTR_NAME));
    final RegionSettings.Type type =
        AbstractSettingsParser.getEnum(RegionSettings.Type.class, attributes.get(ATTR_TYPE));
    final long startAddress = AbstractSettingsParser.getHexLong(attributes.get(ATTR_START));
    final long endAddress = AbstractSettingsParser.getHexLong(attributes.get(ATTR_END));
    final String mode = AbstractSettingsParser.getString(attributes.get(ATTR_MODE));

    InvariantChecks.checkTrue(mode != null && mode.length() == 6);

    final RegionSettings.Mode rwx1 = new RegionSettings.Mode(mode.substring(0, 3));
    final RegionSettings.Mode rwx2 = new RegionSettings.Mode(mode.substring(3, 6));

    return new RegionSettings(name, type, startAddress, endAddress, rwx1, rwx2);
  }
}
