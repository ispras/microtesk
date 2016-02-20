/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link SharedObject} class implements a protocol of copying shared
 * objects.
 * 
 * <p>An object is shared when it is referenced by several other objects.
 * When these objects are copied, the shared object must be copied only
 * once and all objects must use a reference to the same new copy.
 *
 * <p>The protocol is implemented in the following way: The owned of the
 * shared object creates a new copy using a copy constructor. A reference
 * to the new copy is saved in the copied object. Other clients must use
 * the {@code sharedCopy} method to get the reference to the new copy.
 * It is important what all clients get the shared copy before a newer
 * copy is created otherwise they will refer to different instances.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Type of the shared object.
 */

public abstract class SharedObject<T extends SharedObject<T>> {
  /**
   * A fresh copy of this object created by the owner using the
   * copy constructor. This copy is shared with objects that refer
   * to the current object and is used when referencing objects are
   * copied and need to update the reference to the shared object.
   */

  private T copy;

  /**
   * Constructs a new shared object.
   * 
   * <p>The object has no shared copies until it is copied by the owner
   * using the copy constructor.
   */

  protected SharedObject() {
    this.copy = null;
  }

  /**
   * Constructs a copy of a shared object.
   * 
   * <p> This constructor must be called by the owner to make a new
   * copy of the object. The created copy will be shared among the objects
   * that refer to the current object to update the reference when they are
   * copied.
   * 
   * @param other Object to be copied.
   * 
   * @throws IllegalArgumentException if the argument is {@code null}.
   */

  @SuppressWarnings("unchecked")
  protected SharedObject(final SharedObject<T> other) {
    InvariantChecks.checkNotNull(other);

    this.copy = null;
    other.copy = (T) this;
  }

  /**
   * Returns a copy of the object made by its owner.
   * 
   * <p>This method must be used by objects that keep a reference
   * to the object and do not own it to update the reference when they
   * are copied.
   * 
   * @return Copy made by the owner.
   * 
   * @throws IllegalArgumentException if the object has not been copied yet.
   */

  public final T sharedCopy() {
    InvariantChecks.checkNotNull(copy, "Shared copy is unavailable.");
    return copy;
  }
}
