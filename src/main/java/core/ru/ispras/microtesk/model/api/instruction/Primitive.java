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

package ru.ispras.microtesk.model.api.instruction;

import java.util.Map;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.memory.Location;

/**
 * The {@link Primitive} class implements base functionality of addressing modes
 * and operations.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class Primitive {
  /**
   * The {@link Primitive.Factory} interface is a base interface for factories
   * that create instances addressing modes and operations and initialize them
   * with the provided arguments.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public interface Factory<T extends Primitive> {
    /**
     * Creates a primitive instance.
     * 
     * @param args A table of arguments (key is the argument name, value is the argument value).
     * @return The addressing mode object.
     */
    T create(final Map<String, Object> args);
  }

  /**
   * Returns the primitive name.
   * @return Primitive name.
   */
  public final String getName() {
    return getClass().getSimpleName();
  }

  /**
   * Returns assembly format of the specified primitive.
   * 
   * <p>Default implementation is provided to allow using primitives that
   * have no explicitly specified syntax attribute. This method does not do any
   * useful work and should never be called. It is needed only to let inherited
   * classes compile.
   * 
   * @return Assembly text.
   */
  public String syntax() {
    Logger.error(UNDEFINED_ATTR + EMPTY_STR, "syntax", getName());
    return "";
  }

  /**
   * Returns binary representation of the specified primitive.
   * 
   * <p>Default implementation is provided to allow using primitives that
   * have no explicitly specified image attribute. This method does not do any
   * useful work and should never be called. It is needed only to let inherited
   * classes compile.
   * 
   * @return Binary text.
   */
  public String image() {
    Logger.error(UNDEFINED_ATTR + EMPTY_STR + " Primitive size will be 0.", "image", getName());
    return "";
  }

  /**
   * Default implementation of the action attribute. Provided to allow using addressing modes that
   * have no explicitly specified action attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */
  public void action() {
    Logger.error(UNDEFINED_ATTR + "No action will be performed.", "action", getName());
  }

  /**
   * Returns the location the primitive object points to (when initialized with specific
   * parameters). Applicable only to addressing modes that have a return expression.
   * 
   * <p>Default implementation is provided to allow using primitives that do not have
   * a return expression. This method does not do any useful work and should never be called.
   * It is needed only to let inherited classes compile.
   * 
   * @return Memory location.
   */
  public Location access() {
    Logger.error(UNDEFINED_ATTR + "nill is returned.", "access", getName());
    return null;
  }

  private static final String UNDEFINED_ATTR =
      "The '%s' attribute is not defined for the '%s' primitive.";

  private static final String EMPTY_STR =
      " An empty string is returned.";
}
