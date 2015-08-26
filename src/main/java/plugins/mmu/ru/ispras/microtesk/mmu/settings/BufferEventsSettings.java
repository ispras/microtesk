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

import java.util.HashSet;
import java.util.Set;

import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.settings.AbstractSettings;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferEventsSettings extends AbstractSettings {
  public static final String TAG = String.format("%s-bufferEvents", MmuSettings.TAG_PREFIX);

  public static enum Values {
    HIT,
    MISS,
    ALL,
    NONE
  }

  private final String name;
  private final Set<BufferAccessEvent> values = new HashSet<>();

  public BufferEventsSettings(final String name, final Values values) {
    super(TAG);

    this.name = name;

    switch (values) {
      case HIT:
        this.values.add(BufferAccessEvent.HIT);
        break;
      case MISS:
        this.values.add(BufferAccessEvent.MISS);
        break;
      case ALL:
        this.values.add(BufferAccessEvent.HIT);
        this.values.add(BufferAccessEvent.MISS);
        break;
      default:
        break;
    }
  }

  @Override
  public final String getName() {
    return name;
  }

  public final Set<BufferAccessEvent> getValues() {
    return values;
  }

  @Override
  public String toString() {
    return String.format("%s=(%s, %s)", getTag(), name, values);
  }
}
