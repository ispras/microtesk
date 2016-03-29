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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link SharedObject} class implements a protocol of copying shared
 * objects.
 * 
 * <p>An object is shared when it is referenced by several other objects.
 * When these objects are copied, the shared object must be copied only
 * once and all objects must use a reference to the same new copy.
 *
 * <p>The protocol is implemented in the following way: The owner of the
 * shared object creates a new copy using the copy constructor. A reference
 * to the new copy is saved in the copied object. Other clients must use
 * the {@link #sharedCopy} method to get the reference to the new copy.
 * It is important what all clients get the shared copy before a newer
 * copy is created otherwise they will refer to different instances.
 * 
 * <p> In situations when it is not obvious which object is the owner,
 * the {@link #copy(Object)} method must be called. It takes a reference
 * to the client object that requests copying. If the client is the owner,
 * a new copy is created. Otherwise, a shared copy is returned. 
 * Objects which have not been previously copied do not have an owner.
 * Ownership is assigned to the client which first requested copying.
 * The reference to the owner is stored in the copied object and is
 * updated when a new copy is created.
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
   * Reference to the owner, the object that can create new copies.
   * This information is needed to decide whether to create a new copy
   * or to return a shared copy.
   */

  private Object owner;

  /**
   * Constructs a new shared object.
   * 
   * <p>The object has no shared copies until it is copied by the owner
   * using the copy constructor.
   * 
   * <p> The object has no owner until it is copied for the first time.
   * The client which requested copying first becomes the owner.
   */

  protected SharedObject() {
    this.copy = null;
    this.owner = null;
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
    this.owner = null;
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

  /**
   * Returns a list that stores shared copies of objects in the specified list.
   * 
   * @param objects List of objects to be copied.
   * @return List that stores shared copies of the specified objects.
   * 
   * @throws IllegalArgumentException if any of the objects has not been copied yet.
   */

  public static <T extends SharedObject<T>> List<T> sharedCopyAll(final List<T> objects) {
    InvariantChecks.checkNotNull(objects);

    if (objects.isEmpty()) {
      return Collections.emptyList();
    }

    final List<T> result = new ArrayList<>(objects.size());
    for (final T object : objects) {
      result.add(object.sharedCopy());
    }

    return result;
  }

  /**
   * Creates a shared copy of the object if the client is its owner.
   * If the object has no owner, the client becomes its owner.
   * If the client is not its owner, a shared copy is returned.
   * 
   * @param client Object that request a copy.
   * @return Copy of the object.
   */

  public final T copy(final Object client) {
    InvariantChecks.checkNotNull(client);

    if (copy != null && owner != client) {
      return sharedCopy();
    }

    this.owner = client;
    return copy();
  }

  /**
   * Creates a new full copy of the object.
   * This method must call the {@link #SharedObject(SharedObject)}
   * copy constructor in order to publish a shared copy.
   * 
   * @return New full copy of the object.
   */
  public abstract T copy();
}
