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
import java.util.EnumMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.MicroTESK;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.utils.FileUtils;
import ru.ispras.microtesk.utils.StringUtils;

/**
 * The {@link TemplateTest} class is a base class for all JUnit test
 * cases that run test templates and check the results.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class TemplateTest implements Logger.Listener {
  private final String arch;
  private final String path;
  private final Map<Option, String> options;

  public TemplateTest(final String arch, final String path) {
    InvariantChecks.checkNotNull(arch);
    InvariantChecks.checkNotNull(path);

    this.arch = arch;
    this.path = path;
    this.options = new EnumMap<>(Option.class);

    setCommandLineOption(Option.GENERATE);
  }

  public final void setVerbose(final boolean value) {
    setCommandLineOption(Option.VERBOSE, value ? "" : null);
  }

  public final void setCommandLineOption(final Option option) {
    setCommandLineOption(option, "");
  }

  public void setCommandLineOption(final Option option, final String value) {
    InvariantChecks.checkNotNull(option);
    options.put(option, value);
  }

  private void setDefaultCodeFileNamePrefix(final String file) {
    final String fileName = FileUtils.getShortFileNameNoExt(file);
    if (!options.containsKey(Option.CODE_PRE)) {
      setCommandLineOption(Option.CODE_PRE, fileName);
    }
  }

  public final Statistics run(final String file) {
    InvariantChecks.checkNotNull(file);
    setDefaultCodeFileNamePrefix(file);

    Logger.setListener(this);
    MicroTESK.main(makeArgs(file));
    Logger.setListener(null);

    return TestEngine.getInstance().getStatistics();
  }

  private String[] makeArgs(final String file) {
    final ArrayList<String> args = new ArrayList<>();

    for (final Map.Entry<Option, String> entry : options.entrySet()) {
      final Option option = entry.getKey();
      final String value = entry.getValue();

      if (null != value) {
        args.add("-" + option.getShortName());
      }

      if (null != value && !value.isEmpty()) {
        args.add(value);
      }
    }

    args.add(arch);
    args.add(path + "/" + file);

    Logger.message("Command line: " + StringUtils.toString(args, " "));
    return args.toArray(new String[args.size()]);
  }
}
