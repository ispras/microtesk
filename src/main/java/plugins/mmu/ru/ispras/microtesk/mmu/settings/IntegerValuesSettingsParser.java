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

import java.math.BigInteger;
import java.util.Map;

import ru.ispras.microtesk.settings.AbstractSettingsParser;

/**
 * {@link IntegerValuesSettingsParser} implements a parser of {@link IntegerValuesSettings}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerValuesSettingsParser extends AbstractSettingsParser<IntegerValuesSettings> {
  public static final String ATTR_NAME = "name";
  public static final String ATTR_MIN = "min";
  public static final String ATTR_MAX = "max";

  public IntegerValuesSettingsParser() {
    super(IntegerValuesSettings.TAG);

    addParser(new IncludeSettingsParser());
    addParser(new ExcludeSettingsParser());
  }

  @Override
  public IntegerValuesSettings createSettings(final Map<String, String> attributes) {
    final String name = AbstractSettingsParser.getString(attributes.get(ATTR_NAME));
    final BigInteger min = AbstractSettingsParser.getHexBigInteger(attributes.get(ATTR_MIN));
    final BigInteger max = AbstractSettingsParser.getHexBigInteger(attributes.get(ATTR_MAX));

    return new IntegerValuesSettings(name, min, max);
  }
}
