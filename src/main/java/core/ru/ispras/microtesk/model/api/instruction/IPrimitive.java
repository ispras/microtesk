/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link IPrimitive} interface is a base interface for OP (describes an operation)
 * and MODE (describes an addressing mode) nML primitives.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public interface IPrimitive {
  /**
   * Returns textual representation of the specified primitive.
   * 
   * @return Text value.
   */

  String syntax();

  /**
   * Returns binary representation of the specified primitive.
   * 
   * @return Binary text.
   */

  String image();

  /**
   * Runs the action code associated with the primitive.
   */

  void action();

  /**
   * The {@link IPrimitive.IFactory} interface is a base interface for factories that create
   * instances of nML primitives (addressing modes and operations) initialize them with
   * the provided arguments.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */

  public interface IFactory<T extends IPrimitive> {
    /**
     * Creates an addressing mode object.
     * 
     * @param args A table of arguments (key is the argument name, value is the argument value).
     * @return The addressing mode object.
     */

    T create(final Map<String, Object> args);
  }
}
