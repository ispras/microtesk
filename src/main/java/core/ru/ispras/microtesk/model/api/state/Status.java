/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Status {
  // Prototypes for standard status flags
  public static final Status CTRL_TRANSFER = new Status("__CTRL_TRANSFER", 0);
  public static final Map<String, Status> STANDARD_STATUSES = createStandardStatuses();

  private final String name;
  private final int defaultValue;
  private int value;

  public Status(final String name, final int defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.value = defaultValue;
  }

  public String getName() {
    return name;
  }

  public int get() {
    return value;
  }

  public void set(final int value) {
    this.value = value;
  }

  public void reset() {
    value = defaultValue;
  }

  public int getDefault() {
    return defaultValue;
  }

  private static final Map<String, Status> createStandardStatuses() {
    final Map<String, Status> result = new HashMap<>();
    result.put(CTRL_TRANSFER.getName(), CTRL_TRANSFER);
    return Collections.unmodifiableMap(result);
  }
}
