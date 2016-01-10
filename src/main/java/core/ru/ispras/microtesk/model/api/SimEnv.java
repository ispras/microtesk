/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link SimEnv} class implements the simulator runtime environment.
 * It provides a set of methods to be used by all kinds of simulation models
 * to control the simulation process.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class SimEnv {
  private SimEnv() {}

  public static void exception(final String text) {
    Logger.debug("Exception was raised: " + text);
    throw new IsaException(text);
  }

  public static void trace(final String format, final Object... args) {
    Logger.debug(format, args);
  }

  public static void unpredicted() {
    throw new GenerationAbortedException(
        "Unpredicted state was reached during instruction call simulation");
  }

  public static void undefined() {
    throw new GenerationAbortedException(
        "Undefined state was reached during instruction call simulation");
  }

  public static void mark(final String name) {
    //Logger.debug("Mark \"%s\" was reached", name);
  }
}
