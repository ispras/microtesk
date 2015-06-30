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
 * {@link GeneratorSettings} represents generator settings.
 * 
 * <p>The settings contain standard and user-defined sections. Each standard section has a special
 * getter. User-defined section are accessed via the standard {@code get}.</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GeneratorSettings extends AbstractSettings {
  public static final String TAG = "settings";

  public GeneratorSettings() {
    super(TAG);
  }

  public MemorySettings getMemory() {
    return getSingle(MemorySettings.TAG);
  }

  public AllocationSettings getAllocation() {
    return getSingle(AllocationSettings.TAG);
  }

  public DelaySlotSettings getDelaySlot() {
    return getSingle(DelaySlotSettings.TAG);
  }

  public ExtensionsSettings getExtensions() {
    return getSingle(ExtensionsSettings.TAG);
  }
}
