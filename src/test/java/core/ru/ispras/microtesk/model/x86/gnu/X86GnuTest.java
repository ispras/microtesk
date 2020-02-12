/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

package ru.ispras.microtesk.model.x86.gnu;

import org.junit.Assert;

import ru.ispras.castle.util.Logger;
import ru.ispras.microtesk.model.x86.X86Test;
import ru.ispras.microtesk.test.Statistics;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic class for tests that are related to GNU syntax x86 test program generation.
 *
 * @author <a href="mailto:smolov@ispras.ru">Sergey Smolov</a>
 */
public abstract class X86GnuTest extends X86Test {

  /* x86 GNU toolchain parameters. */

  /**
   * x86 toolchain path environment variable name.
   */
  private static final String GNU_TCHAIN_PATH = "X86_GNU_TCHAIN";

  /**
   * x86 toolchain path environment variable.
   */
  private static final String TCHAIN_PATH = System.getenv(GNU_TCHAIN_PATH);

  /**
   * x86 Linux GNU toolchain components common prefix.
   */
  private static final String TCHAIN_PREFIX = "x86_64-linux-gnu-";

  /**
   * Default constructor.
   */
  public X86GnuTest() {
    super("x86gnu");
  }

  @Override
  public Statistics run(final String file) {
    return super.run(file);
  }

  @Override
  protected String getTchainPath() {
    return TCHAIN_PATH;
  }

  @Override
  protected String getTchainPrefix() {
    return TCHAIN_PREFIX;
  }

  @Override
  protected File compile(final File program, final Collection<File> auxFiles) {

    Logger.message(String.format("Start compilation of %s ...", program.getName()));
    setPhase(TestPhase.COMPILATION);

    /* Check whether toolchain has been installed. */

    if (getTchainPath() == null || getTchainPath().isEmpty()) {
      Assert.fail(
          String.format("Can't find toolchain: '%s' env var points to null!", GNU_TCHAIN_PATH));
    }

    final File asm = getToolchainBinary(getTchainPrefix(), "as");
    final File linker = getToolchainBinary(getTchainPrefix(), "ld");

    /* asm -> obj */
    runCommand(
        asm,
        true,
        program.getAbsolutePath(),
        "-o",
        getFile(getNameNoExt(program), "o"));

    for (final File file : auxFiles) {
      runCommand(
          asm,
          true,
          file.getAbsolutePath(),
          "-o",
          getFile(getNameNoExt(file), "o"));
    }

    /* obj -> elf */
    final String[] objPaths = getObjFiles(program, auxFiles);
    final List<String> linkerArgs = new LinkedList<>();
    Collections.addAll(linkerArgs, objPaths);

    final String linkerScriptPath = getLinkerScript(new File(getTestDirPath()));
    if (linkerScriptPath.isEmpty()) {
      linkerArgs.add("-Ttext");
      linkerArgs.add("0x7c00");
    } else {
      linkerArgs.add("-T");
      linkerArgs.add(linkerScriptPath);
    }

    linkerArgs.add("--oformat");
    linkerArgs.add("binary");
    linkerArgs.add("-o");
    linkerArgs.add(getFile(getNameNoExt(program), "elf"));
    runCommand(linker, true, linkerArgs.toArray(new String[0]));

    final File elfImage = new File(getElf(program));

    Logger.message("done.");
    return elfImage;
  }
}
