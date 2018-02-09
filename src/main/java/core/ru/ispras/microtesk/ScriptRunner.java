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

import java.nio.file.Paths;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;

/**
 * The {@link ScriptRunner} class runs test template scripts with corresponding scripting engines.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ScriptRunner {
  private ScriptRunner() {}

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
    runRuby(options, templateFile);
  }

  private static void runRuby(final Options options, final String templateFile) throws Throwable {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(templateFile);

    final String homeDir = SysUtils.getHomeDir();
    final String rubyMainPath = Paths.get(homeDir, "lib", "ruby", "microtesk.rb").toString();

    // Number of threads used by JRuby can be limited to prevent hitting OS limit.
    org.jruby.util.cli.Options.THREADPOOL_MAX.force(
        options.getValue(Option.JRUBY_THREAD_POOL_MAX).toString());

    final ScriptingContainer container = new ScriptingContainer();
    container.setArgv(new String[] {templateFile});

    // To make sure that THREADPOOL_MAX has an expected value.
    //Logger.message("THREADPOOL_MAX=%d", org.jruby.util.cli.Options.THREADPOOL_MAX.load());

    try {
      container.runScriptlet(PathType.ABSOLUTE, rubyMainPath);
    } catch (final org.jruby.embed.EvalFailedException e) {
      // JRuby wraps exceptions that occur in Java libraries it calls into
      // EvalFailedException. To handle them correctly, we need to unwrap them.
      throw e.getCause();
    }
  }
}
