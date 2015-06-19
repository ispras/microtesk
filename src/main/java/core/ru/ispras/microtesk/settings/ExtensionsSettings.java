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

import java.util.ArrayList;
import java.util.Collection;

/**
 * {@link ExtensionsSettings} represents memory regions being accessed by test programs.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ExtensionsSettings extends AbstractSettings {
  public static final String TAG = "extensions";

  public ExtensionsSettings() {
    super(TAG);
  }

  public Collection<ExtensionSettings> getExtensions() {
    final Collection<ExtensionSettings> result = new ArrayList<>();

    for (final AbstractSettings extension : get(ExtensionSettings.TAG)) {
      result.add((ExtensionSettings) extension);
    }

    return result;
  }
}
