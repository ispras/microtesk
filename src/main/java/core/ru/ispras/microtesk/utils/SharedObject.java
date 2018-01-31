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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
 * to the new copy is saved in the table of shared objects. Other clients
 * must use the {@link #sharedCopy} method to get the reference to the new copy.
 * It is important what all clients get the shared copy before a newer
 * copy is created otherwise they will refer to different instances.
 *
 * <p> In situations when it is not obvious which object is the owner,
 * the {@link #getCopy} method can be used. It returns an existing shared
 * copy if it is available or creates a new shared copy otherwise. To free
 * existing shared copies, the {@link #freeSharedCopies} must be used.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <T> Type of the shared object.
 */
public abstract class SharedObject<T extends SharedObject<T>> {
  /**
   * Table shared copies. Key is original object, value is its shared copy.
   */
  private static Map<Object, Object> sharedObjects = null;

  /**
   * Constructs a new shared object.
   *
   * <p>No shared copies a published until an object is copied.
   */
  protected SharedObject() {
  }

  /**
   * Constructs a copy of a shared object.
   *
   * <p> This constructor must be called by the owner in order to publish
   * a shared copy of the object.
   *
   * @param other Object to be copied.
   *
   * @throws IllegalArgumentException if the argument is {@code null}.
   */
  protected SharedObject(final SharedObject<T> other) {
    InvariantChecks.checkNotNull(other);
    publishSharedCopy(other, this);
  }

  /**
   * Frees all shared objects.
   */
  public static void freeSharedCopies() {
    sharedObjects = null;
  }

  /**
   * Returns a shared copy of the object made by its owner.
   *
   * <p>This method must be used by objects that keep a reference
   * to the object and do not own it to update the reference when they
   * are copied.
   *
   * @return Shared copy of the object.
   *
   * @throws IllegalArgumentException if no shared copy is available yet.
   */
  public final T sharedCopy() {
    final T copy = getSharedCopyFor(this);
    InvariantChecks.checkNotNull(copy, "Shared copy is unavailable.");
    return copy;
  }

  /**
   * Returns a list that stores shared copies of objects in the specified list.
   *
   * @param <T> Type of objects to be copied.
   * @param objects List of objects to be copied.
   * @return List that stores shared copies of the specified objects.
   *
   * @throws IllegalArgumentException if any of the objects has no shared copy.
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
   * Returns a shared copy of the object if it is available. Otherwise,
   * creates and returns a new shared copy.
   *
   * @return Copy of the object.
   *
   * @throws IllegalArgumentException if the newly created shared copy was not published.
   */
  public final T getCopy() {
    final T sharedCopy = getSharedCopyFor(this);

    if (null != sharedCopy) {
      return sharedCopy;
    }

    final T newCopy = newCopy();
    InvariantChecks.checkNotNull(getSharedCopyFor(this), "Shared copy is not published.");

    return newCopy;
  }

  /**
   * Creates a new full copy of the object.
   * This method must call the {@link #SharedObject(SharedObject)}
   * copy constructor in order to publish a shared copy.
   *
   * @return New full copy of the object.
   */
  public abstract T newCopy();

  /**
   * Publishes a shared copy of an object. If a shared copy of the object
   * has already been published, it is replaced with the new one.
   *
   * @param original Original object.
   * @param copy Copy of the original object to be shared.
   *
   * @throws IllegalArgumentException if any of the arguments is {@code null}.
   */
  protected static void publishSharedCopy(final Object original, final Object copy) {
    InvariantChecks.checkNotNull(original);
    InvariantChecks.checkNotNull(copy);

    if (null == sharedObjects) {
      sharedObjects = new IdentityHashMap<>();
    }

    sharedObjects.put(original, copy);
  }

  /**
   * Returns the most recent shared copy for the specified object or {@null}
   * if no shared copy is available.
   *
   * @param original Original object associated with the shared copy.
   * @return Shared copy of the given object or {@code null} if no shared copy
   *         is available.
   */
  @SuppressWarnings("unchecked")
  private static <U> U getSharedCopyFor(final Object original) {
    InvariantChecks.checkNotNull(original);

    if (null == sharedObjects) {
      return null;
    }

    final Object copy = sharedObjects.get(original);
    if (null == copy) {
      return null;
    }

    InvariantChecks.checkTrue(original.getClass().equals(copy.getClass()));
    return (U) copy;
  }
}
