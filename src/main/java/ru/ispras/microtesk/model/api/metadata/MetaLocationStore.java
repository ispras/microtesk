/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaLocationStore.java, Nov 15, 2012 2:53:11 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

/**
 * The MetaLocationStore class describes memory resources of the processor (as registers and memory
 * store locations).
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaLocationStore implements MetaData {
  private final String name;
  private final int count;

  public MetaLocationStore(String name, int count) {
    if (null == name) {
      throw new NullPointerException();
    }

    if (count <= 0) {
      throw new IllegalArgumentException(String.format("%d <= 0", count));
    }

    this.name = name;
    this.count = count;
  }

  /**
   * Returns the name of the resource.
   * 
   * @return Memory resource name.
   */

  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the count of items in the memory store.
   * 
   * @return Memory store item count.
   */

  public int getCount() {
    return count;
  }
}
