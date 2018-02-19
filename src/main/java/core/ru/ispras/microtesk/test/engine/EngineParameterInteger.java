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

/**
 * {@link EngineParameterInteger} represents an integer-value engine parameter.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class EngineParameterInteger extends EngineParameter<Integer> {
  public EngineParameterInteger(final String name, final int defaultValue) {
    super(name, Integer.valueOf(defaultValue));
  }

  @Override
  public Integer getValue(final Object option) {
    final Number number = (option instanceof Number)
        ? (Number) option
        : Integer.parseInt(option.toString(), 10);

    return number.intValue();
  }
}
