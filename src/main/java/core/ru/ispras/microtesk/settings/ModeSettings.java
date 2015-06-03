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

/**
 * {@link ModeSettings} describes an addressing mode's allocation table.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ModeSettings extends AbstractSettings {
  public static final String TAG = "mode";

  private final String name;

  public ModeSettings(final String name) {
    super(TAG);

    this.name = name;
  }

  public String getName() {
    return name;
  }

  public RangeSettings getRange() {
    return getSingle(RangeSettings.TAG);
  }

  @Override
  public String toString() {
    return String.format("%s=%s", TAG, getRange().getValues());
  }
}
