/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.codegen;

import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.misc.STMessage;

import ru.ispras.microtesk.Logger;

final class ErrorListener implements STErrorListener {
  private static ErrorListener instance = null;

  public static ErrorListener get() {
    if (null == instance) {
      instance = new ErrorListener();
    }
    return instance;
  }

  private ErrorListener() {}

  private void report(final String msg) {
    Logger.error(msg);
  }

  @Override
  public void compileTimeError(final STMessage msg) {
    report("Run-time error: " + msg);
  }

  @Override
  public void runTimeError(final STMessage msg) {
    report("Internal error: " + msg);
  }

  @Override
  public void IOError(final STMessage msg) {
    report("Compile-time error: " + msg);
  }

  @Override
  public void internalError(final STMessage msg) {
    report("I/O error: " + msg);
  }
}
