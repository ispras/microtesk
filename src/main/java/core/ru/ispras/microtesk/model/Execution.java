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

package ru.ispras.microtesk.model;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link Execution} class implements the execution environment.
 * It provides a set of methods to be used by all kinds of executable models
 * to control the execution process.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Execution {
  private Execution() {}

  private static boolean assertionsEnabled = false;

  public static void exception(final String text) {
    Logger.debug("Exception was raised: %s", text);
    throw new ExecutionException(text);
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

  public static void assertion(final boolean condition) {
    assertion(condition, null);
  }

  public static void assertion(final boolean condition, final String message) {
    if (condition || !assertionsEnabled) {
      return;
    }

    final StringBuilder sb = new StringBuilder("Assertion failed");
    if (null != message) {
      sb.append(": ");
      sb.append(message);
    }

    throw new GenerationAbortedException(sb.toString());
  }

  public static void setAssertionsEnabled(final boolean value) {
    assertionsEnabled = value;
  }

  public abstract static class InternalVariable {
    public abstract int load();

    public abstract void store(final int value);

    public final void store(final Data data) {
      store(data.bigIntegerValue().intValue());
    }
  }

  public static final InternalVariable float_exception_flags = new InternalVariable() {
    @Override
    public int load() {
      return Data.getFloatExceptionFlags();
    }

    @Override
    public void store(final int value) {
      Data.setFloatExceptionFlags(value);
    }
  };

  public static final InternalVariable float_rounding_mode = new InternalVariable() {
    @Override
    public int load() {
      return Data.getFloatRoundingMode();
    }

    @Override
    public void store(final int value) {
      Data.setFloatRoundingMode(value);
    }
  };
}
