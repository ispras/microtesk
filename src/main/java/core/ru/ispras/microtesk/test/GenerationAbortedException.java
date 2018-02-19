/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import java.io.StringWriter;

public final class GenerationAbortedException extends RuntimeException {
  private static final long serialVersionUID = -7676366332465641144L;

  public GenerationAbortedException(final String message) {
    super(message);
  }

  public GenerationAbortedException(final Throwable cause) {
    super(makeMessage(null, cause));
  }

  public GenerationAbortedException(final String message, final Throwable cause) {
    super(makeMessage(message, cause));
  }

  private static String makeMessage(final String message, final Throwable cause) {
    final StringWriter writer = new StringWriter();

    if (null != message) {
      writer.append(message);
    }

    if (null != cause.getMessage()) {
      writer.append(cause.getMessage());
    }

    cause.printStackTrace(new java.io.PrintWriter(writer));
    return writer.toString();
  }
}
