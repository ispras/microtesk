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

package ru.ispras.microtesk.model.api;

import java.util.Deque;
import java.util.LinkedList;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.memory.Location;

/**
 * The {@link IsaPrimitive} class implements base functionality of addressing modes
 * and operations.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class IsaPrimitive {
  /** Tracks execution of primitives. */
  private static final Deque<IsaPrimitive> CALL_STACK = new LinkedList<>();

  /**
   * Returns the name of the currently executed primitive or an empty
   * string if no primitive is being executed.
   * 
   * @return Name of the executed primitive.
   */
  public static String getCurrentOperation() {
    return CALL_STACK.isEmpty() ? "" : CALL_STACK.peek().getName();
  }

  /**
   * Returns the primitive name.
   * 
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
  public String syntax(final ProcessingElement processingElement) {
    reportUndefined("syntax", "Empty string will be returned");
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
  public String image(final ProcessingElement processingElement) {
    reportUndefined("image", "Empty string will be returned, primitive size will be 0");
    return "";
  }

  /**
   * Runs execution of the current primitive's action.
   */
  public final void execute(final ProcessingElement processingElement) {
    try {
      CALL_STACK.push(this);
      action(processingElement);
    } finally {
      CALL_STACK.pop();
    }
  }

  /**
   * Runs the action associated with the primitive.
   * 
   * <p>Default implementation is provided to allow using primitives that have no
   * explicitly specified action attribute. This method does not do any useful work
   * and should never be called. It is needed only to let inherited classes compile.
   */
  protected void action(final ProcessingElement processingElement) {
    reportUndefined("action", "No action will be performed");
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
  public Location access(final ProcessingElement processingElement) {
    reportUndefined("access", "null will be returned");
    return null;
  }

  private void reportUndefined(final String attrName, final String message) {
    Logger.error(
        "The '%s' attribute is undefined for the '%s' primitive. %s.",
        attrName,
        getName(),
        message
        );
  }
}
