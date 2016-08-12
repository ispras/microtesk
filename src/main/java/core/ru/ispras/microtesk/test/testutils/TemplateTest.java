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

package ru.ispras.microtesk.test.testutils;

import java.util.ArrayList;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.MicroTESK;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.TestEngine;

/**
 * The {@link TemplateTest} class is a base class for all JUnit test
 * cases that run test templates and check the results.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class TemplateTest implements Logger.Listener {
  private final String arch;
  private final String path;
  private boolean verbose;

  public TemplateTest(final String arch, final String path) {
    InvariantChecks.checkNotNull(arch);
    InvariantChecks.checkNotNull(path);

    this.arch = arch;
    this.path = path;
    this.verbose = false;
  }

  public void setVerbose(boolean value) {
    this.verbose = value;
  }

  public final Statistics run(final String file) {
    InvariantChecks.checkNotNull(file);

    Logger.setListener(this);
    MicroTESK.main(makeArgs(file));
    Logger.setListener(null);

    return TestEngine.getInstance().getStatistics();
  }

  private String[] makeArgs(final String file) {
    final ArrayList<String> args = new ArrayList<>();

    args.add("-g");
    if (verbose) {
      args.add("-v");
    }

    args.add(arch);
    args.add(path + "/" + file);

    return args.toArray(new String[args.size()]);
  }
}
