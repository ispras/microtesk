/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.x86;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import ru.ispras.castle.util.FileUtils;
import ru.ispras.castle.util.Logger;
import ru.ispras.castle.util.Logger.EventType;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.test.Statistics;
import ru.ispras.microtesk.test.testutils.TemplateTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public abstract class X86Test extends TemplateTest {

  /**
   * The test program name prefix.
   */
  private String programPrefix;

  /**
   * The path to directory containing test program.
   */
  private String testDirPath;

  /**
   * The specification model name.
   */
  private String modelName;

  /**
   * Path to test results.
   */
  private static final String TEST_PATH = System.getenv("TEST_PATH");

  /**
   * Test programs extension.
   */
  private static final String EXT = "s";

  /**
   * Linker script file extension.
   */
  private static final String LINKER_SCRIPT_EXT = "ld";

  /* QEMU4V parameters. */

  /**
   * QEMU binary name.
   */
  private static final String QEMU_BIN = "qemu-system-i386";

  /**
   * QEMU location environment variable name.
   */
  private static final String QEMU_VAR = "QEMU4V_PATH";

  /**
   * QEMU location environment variable.
   */
  private static final String QEMU_PATH = System.getenv(QEMU_VAR);

  /**
   * Timeout for QEMU execution (in milliseconds).
   */
  private static final int QEMU_TIMEOUT_MILLIS = 5000;

  /**
   * Default constructor for tests are related to x86 moel with the specified name.
   * @param modelName The x86 architecture model name.
   */
  public X86Test(final String modelName) {
    super(
        modelName,
        "src/main/arch/demo/x86/templates"
        );
    setModelName(modelName);
  }

  @Override
  public Statistics run(final String file) {
    setProgramPrefix(file);

    final String fileDir = FileUtils.getFileDir(file);
    final Path testDirPath = null != fileDir
        ? Paths.get(TEST_PATH, fileDir, getModelName(), getProgramPrefix())
        : Paths.get(TEST_PATH, getModelName(), getProgramPrefix());
    setTestDirPath(testDirPath);

    setCommandLineOption(Option.TRACER_LOG);
    setCommandLineOption(Option.PROGRAM_LENGTH_LIMIT, "500");
    setCommandLineOption(Option.OUTPUT_DIR, getTestDirPath());
    setCommandLineOption(Option.CODE_FILE_EXTENSION, EXT);
    setCommandLineOption(Option.DATA_FILE_EXTENSION, EXT);
    return super.run(file);
  }

  private void setModelName(final String name) {
    this.modelName = name;
  }

  private String getModelName() {
    return this.modelName;
  }

  private void setProgramPrefix(final String file) {
    this.programPrefix = FileUtils.getShortFileNameNoExt(file);
  }

  private void setTestDirPath(final Path testDirPath) {
    this.testDirPath = testDirPath.toString();
  }

  private String getProgramPrefix() {
    return this.programPrefix;
  }

  protected String getTestDirPath() {
    return this.testDirPath;
  }

  /**
   * Compiles generated test programs and runs them on emulator.
   */
  @After
  public void compileAndEmulate() {

    /* If toolchain is installed, loop on prefix-named test programs,
     * compile every test program, if it fails, throw error message. */
    final File testDir = new File(getTestDirPath());

    if (!testDir.exists() || !testDir.isDirectory()) {
      Assert.fail(String.format("Can't find '%s' test program directory.", getTestDirPath()));
    }

    final File[] files = testDir.listFiles();

    final Collection<File> auxFiles = new LinkedHashSet<>();
    final Collection<File> tests = new LinkedHashSet<>();

    Assert.assertNotNull("No test programs are generated from this template.", files);

    for (final File file : files) {

      final String fileName = file.getName();
      if (fileName.endsWith(EXT)) {
        if (fileName.startsWith(getProgramPrefix())) {
          tests.add(file);
        } else {
          auxFiles.add(file);
        }
      }
    }

    for (final File program : tests) {
      final File image = compile(program, auxFiles);
      emulate(image);
    }
  }

  private void emulate(final File image) {

    /* If QEMU is installed, run the binary image on it. */

    if (QEMU_PATH == null || QEMU_PATH.isEmpty()) {
      Assert.fail(
          String.format(
              "Can't find emulator: '%s' env var doesn't point to '%s' binary.",
              QEMU_VAR,
              QEMU_BIN));
      return;
    }

    final File qemu = new File(String.format("%s/%s", QEMU_PATH, QEMU_BIN));
    checkExecutable(qemu);

    Logger.message("Start emulation ...");
    final String qemuLogName = insertExt(image.getAbsolutePath(), "-qemu.log");

    final String[] qemuArgs = new String[] {
      "-M",
      "pc",
      "-cpu",
      "486",
      "-d",
      "unimp,nochain,in_asm",
      "-nographic",
      "-singlestep",
      //"-trace-log",
      "-D",
      qemuLogName,
      "-hda",
      image.getAbsolutePath()};
    runCommand(qemu, QEMU_TIMEOUT_MILLIS, Collections.singletonList(0), qemuArgs);

    final File qemuLog = new File(qemuLogName);
    final String qemuLogPath = qemuLog.getAbsolutePath();

    Assert.assertTrue(String.format("Can't find QEMU trace: %s", qemuLogPath), qemuLog.exists());
    Assert.assertFalse(String.format("QEMU trace is empty: %s", qemuLogPath), isEmpty(qemuLog));

    Logger.message("done.");
  }

  /**
   * Compiles the specified main program and a collection of auxiliary files.
   * @param program The main program to be compiled.
   * @param auxFiles The collection of auxiliary program files.
   * @return The compiled image file.
   */
  protected abstract File compile(final File program, final Collection<File> auxFiles);

  /**
   * Returns the path to linker script file.
   * @param testDirPath The path to test directory.
   * @return The path to linker script file.
   */
  protected String getLinkerScript(final File testDirPath) {

    String path = "";

    final File[] files = testDirPath.listFiles();
    Assert.assertNotNull("No test programs are generated from this template.", files);

    for (final File file : files) {

      final String fileName = file.getName();
      if (fileName.endsWith(LINKER_SCRIPT_EXT)) {
        path = file.getPath();
        break;
      }
    }

    return path;
  }

  /**
   * Returns the array of paths to files with ".o" extension and from the specified file location.
   * @param program The main program.
   * @param auxFiles The collection of auxiliary files.
   * @return The array of paths to files with ".o" extension and from the specified file location.
   */
  protected String[] getObjFiles(final File program, final Collection<File> auxFiles) {

    final List<File> files = new LinkedList<>();
    files.add(program);
    files.addAll(auxFiles);

    return getFiles(".o", files.toArray(new File[0]));
  }

  /**
   * Returns the array of paths to files with ".elf" extension and from the specified file location.
   * @param program The main program.
   * @return The array of paths to files with ".elf" extension and from the specified file location.
   */
  protected String getElf(final File program) {
    final String[] elfFiles = getFiles(".elf", program);
    return elfFiles[0];
  }

  private String[] getFiles(final String ext, final File ... files) {

    final String[] result = new String[files.length];

    for (int i = 0; i < files.length; i++) {
      final File file = files[i];
      final String pathWithExt = insertExt(file.getAbsolutePath(), ext);

      final File fileWithExt = new File(pathWithExt);
      if (!fileWithExt.exists() || fileWithExt.isDirectory()) {
        Assert.fail(String.format("Can't find: %s", pathWithExt));
      }
      result[i] = fileWithExt.getAbsolutePath();
    }

    return result;
  }

  private static String insertExt(final String path, final String ext) {
    return String.format(
      "%s%s",
      path.substring(0, path.contains(".") ? path.lastIndexOf('.') : path.length()),
      ext);
  }

  /**
   * Runs the specified executable file with the specified parameters.
   * @param cmd The executable command file.
   * @param args The array of auxiliary options.
   */
  protected void runCommand(final File cmd, final String... args) {
    runCommand(cmd, 0, Collections.singletonList(0), args);
  }

  private void runCommand(
      final File cmd,
      final long timeout,
      final Collection<Integer> returnCodes,
      final String ... args) {

    final String[] cmdArray = toArray(cmd, args);
    final String command = StringUtils.join(cmdArray, ' ');

    Logger.message("Command: '%s'", Arrays.toString(cmdArray));

    try {
      final ProcessBuilder builder = new ProcessBuilder(cmdArray);
      builder.inheritIO();
      final Process process = builder.start();

      int exitCode;

      if (timeout > 0) {

        Thread.sleep(timeout);

        try {
          exitCode = process.exitValue();

        } catch (IllegalThreadStateException e) {

          Logger.message("Timeout is expired for: \"%s\"", command);
          process.destroy();
          exitCode = 0;
        }
      } else {
        exitCode = process.waitFor();
        process.destroy();
      }

      if (!returnCodes.contains(exitCode)) {
        Assert.fail(String.format("Process has returned '%d': \"%s\"%n", exitCode, command));
      }
    } catch (final IOException | InterruptedException e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  private String[] toArray(
      final File command,
      final String ... args) {

    final List<String> commands = new LinkedList<>();
    commands.add(command.getAbsolutePath());
    Collections.addAll(commands, args);

    return commands.toArray(new String[0]);
  }

  /**
   * Returns the sub-path of the file with the specified name and extension.
   * <p>The sub-path includes the test directory name also.</p>
   * @param fileNamePrefix The file name without an extension.
   * @param ext The file extension.
   * @return The sub-path of the file with the specified name and extension.
   */
  protected String getFile(final String fileNamePrefix, final String ext) {

    return String.format("%s/%s.%s", getTestDirPath(), fileNamePrefix, ext);
  }

  /**
   * Returns the name of the specified file without an extension.
   * @param file The file.
   * @return The name of the specified file without an extension.
   */
  protected static String getNameNoExt(final File file) {
    return FileUtils.getShortFileNameNoExt(file.getName());
  }

  /**
   * Returns the path to the tool chain.
   * @return The path to the tool chain.
   */
  protected abstract String getTchainPath();

  /**
   * Returns the prefix to the tool chain components.
   * @return The prefix to the tool chain components.
   */
  protected abstract String getTchainPrefix();

  /**
   * Returns the tool chain binary component with the specified first and last parts of the name.
   * @param prefix The first part of the name.
   * @param suffix The last part of the name.
   * @return The tool chain binary component with the specified first and last parts of the name.
   */
  protected File getToolchainBinary(final String prefix, final String suffix) {

    final String path = String.format("%s/bin/%s%s", getTchainPath(), prefix, suffix);
    final File binary = new File(path);

    checkExecutable(binary);
    return binary;
  }

  private static void checkExecutable(final File file) {

    Assert.assertTrue(String.format("Can't find: %s", file.getAbsolutePath()), isExecutable(file));
  }

  private static boolean isExecutable(final File file) {
    return file.exists() && !file.isDirectory() && file.canExecute();
  }

  private static boolean isEmpty(final File file) {

    if (!file.exists()) {
      return true;
    }
    try {
      final BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));

      final String firstLine = reader.readLine();
      reader.close();
      return firstLine == null;

    } catch (final IOException e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
    return false;
  }

  @Override
  public void onEventLogged(final EventType type, final String message) {
    if (EventType.ERROR == type || EventType.WARNING == type) {
      if (!isExpectedError(message)) {
        Assert.fail(message);
      }
    }
  }

  protected boolean isExpectedError(final String message) {
    return message.contains("Failed to load the MMU model");
  }
}
