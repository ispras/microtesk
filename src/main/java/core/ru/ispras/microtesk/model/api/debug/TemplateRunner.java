/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

/**
 * The TemplateRunner class is needed to be able to run and debug test templates in Eclipse.
 * 
 * @author Andrei Tatarnikov
 */

public final class TemplateRunner {
  private TemplateRunner() {}

  private static void runTemplate(List<String> argv) {
    final ScriptingContainer container = new ScriptingContainer();

    final String scriptsPath =
      String.format("%s/dist/libs/ruby/template_processor.rb", System.getProperty("user.dir"));

    container.setArgv(argv.toArray(new String[argv.size()]));
    container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
  }

  public static void main(String[] args) {
    if (args.length < 3) {
      System.out.println(
        "The following arguments are required: <model file>, <design name>, <template file name>");
      return;
    }

    final String models = String.format("%s/%s", System.getProperty("user.dir"), args[0]);
    final String designName = args[1];
    final String templatePath = String.format("%s/%s", System.getProperty("user.dir"), args[2]);

    if (!new File(models).exists()) {
      System.out.printf("The %s model file does not exists.", models);
      return;
    }

    if (!new File(templatePath).exists()) {
      System.out.printf("The %s template file does not exists.", templatePath);
      return;
    }

    final List<String> argv = new ArrayList<String>();

    argv.add(models);
    argv.add(designName);
    argv.add(templatePath);

    for (int index = 3; index < args.length; ++index) {
      argv.add(args[index]);
    }

    runTemplate(argv);
  }
}
