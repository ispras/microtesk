/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link PythonRunner} class runs test template scripts with Jython.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class PythonRunner {
  private PythonRunner() {}

  /**
   * Runs the specified test template to generate a set of test programs.
   *
   * @param options Options that set up the run configuration.
   * @param templateFile Test template to be run.
   *
   * @throws Throwable if any issues occurred during the script run. A special case is
   *         {@link ru.ispras.microtesk.test.GenerationAbortedException} which means that some
   *         of the engines invoked by the script decided to abort generation.
   */
  public static void run(final Options options, final String templateFile) throws Throwable {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(templateFile);

    // TODO
    throw new GenerationAbortedException("Test templates in Python are not yet supported.");
  }
}
