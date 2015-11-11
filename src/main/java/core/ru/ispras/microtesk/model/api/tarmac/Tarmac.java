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

package ru.ispras.microtesk.model.api.tarmac;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import ru.ispras.fortress.util.InvariantChecks;

public final class Tarmac {
  private static final String FILE_PREFIX = "tarmac";
  private static final String FILE_EXTENSION = "log";

  private final String filePrefix;
  private final String fileExtension;
  private int fileCount;
  private PrintWriter fileWritter;

  private static Tarmac instance = null;
  private static boolean enabled = false;

  public static void initialize(final String filePrefix) {
    InvariantChecks.checkTrue(null == instance);
    instance = new Tarmac(null != filePrefix ? filePrefix : FILE_PREFIX);
  }

  public static void shutdown() {
    instance = null;
  }

  public static boolean isEnabled() {
    return null != instance && enabled;
  }

  public static void setEnabled(final boolean value) {
    enabled = value;
  }

  public static String createFile() throws IOException {
    if (null != instance) {
      return instance.create();
    }
    return null;
  }

  public static void closeFile() {
    if (null != instance) {
      instance.close();
    }
  }

  public static void addRecord(final Record record) {
    if (null != instance) {
      instance.print(record);
    }
  }

  private Tarmac(final String filePrefix) {
    InvariantChecks.checkNotNull(filePrefix);

    this.filePrefix = filePrefix;
    this.fileExtension = FILE_EXTENSION;
    this.fileCount = 0;
    this.fileWritter = null;
  }

  private String create() throws IOException {
    close();
    Record.resetInstructionCount();

    final String fileName = String.format(
        "%s_%04d.%s",
        filePrefix,
        fileCount++,
        fileExtension
        );

    fileWritter = new PrintWriter(new FileWriter(fileName));
    return fileName;
  }

  private void close() {
    if (null != fileWritter) {
      fileWritter.close();
    }
  }

  private void print(final Record record) {
    InvariantChecks.checkNotNull(fileWritter, "Tarmac file is not open.");
    fileWritter.println(record.toString());
  }
}
