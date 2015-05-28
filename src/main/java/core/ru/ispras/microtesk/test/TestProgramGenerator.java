/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.io.File;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

import ru.ispras.fortress.solver.SolverId;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.translator.nml.coverage.TestBase;

/**
 * TODO: Temporary intermediate implementation.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class TestProgramGenerator {
  private String modelName;
  private String fileName;
  private int randomSeed;
  private boolean isRandomSeedSet;

  public TestProgramGenerator() {
    this.modelName = "";
    this.fileName = "";
    this.randomSeed = 0;
    this.isRandomSeedSet = false;
  }

  public void setModelName(final String value) {
    modelName = value;
  }

  public void setRandomSeed(final int value) {
    randomSeed = value;
    isRandomSeedSet = true;
  }

  public void setFileName(final String value) {
    fileName = value;
  }

  public boolean isRandomSeedSet() {
    return isRandomSeedSet;
  }

  public int getRandomSeed() {
    return randomSeed;
  }

  public void setBranchExecutionLimit(int branchExecutionLimit) {
    TestEngine.setBranchExecutionLimit(branchExecutionLimit);
  }

  public void setSolver(final String solverName) {
    if ("z3".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.Z3_TEXT);
    } else if ("cvc4".equalsIgnoreCase(solverName)) {
      TestBase.setSolverId(SolverId.CVC4_TEXT);
    } else {
      Logger.warning("Unknown solver: %s. Default solver will be used.", solverName);
    }
  }

  public void generate(final String templateFile) throws Throwable {
    try {
      if (null != fileName && !fileName.isEmpty()) {
        runTemplate(modelName, templateFile, fileName);
      } else {
        runTemplate(modelName, templateFile);
      }
    } catch (GenerationAbortedException e) {
      Logger.error(e.getMessage());
      new File(fileName).delete();
    }
  }

  private static void runTemplate(final String... argv) throws Throwable {
    final ScriptingContainer container = new ScriptingContainer();

    final String scriptsPath = String.format(
        "%s/lib/ruby/microtesk.rb", System.getenv("MICROTESK_HOME"));

    container.setArgv(argv);

    try {
      container.runScriptlet(PathType.ABSOLUTE, scriptsPath);
    } catch(org.jruby.embed.EvalFailedException e) {
      // JRuby wraps exceptions that occur in Java libraries it calls into
      // EvalFailedException. To handle them correctly, we need to unwrap them.
      throw e.getCause();
    }
  }
}
