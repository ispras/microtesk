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

package ru.ispras.microtesk.utils;

import java.io.File;

import ru.ispras.fortress.util.InvariantChecks;

public final class FileUtils {
  private FileUtils() {}

  public static String getFileExtension(final String fileName) {
    InvariantChecks.checkNotNull(fileName);

    final int lastIndexOf = fileName.lastIndexOf(".");
    return lastIndexOf != -1 ? fileName.substring(lastIndexOf) : "";
  }

  public static String getShortFileName(final String fileName) {
    InvariantChecks.checkNotNull(fileName);
    return new File(fileName).getName();
  }

  public static String getShortFileNameNoExt(final String fileName) {
    InvariantChecks.checkNotNull(fileName);

    final String shortFileName = getShortFileName(fileName);
    final int dotPos = shortFileName.lastIndexOf('.');

    if (-1 == dotPos) {
      return shortFileName.toLowerCase();
    }

    return shortFileName.substring(0, dotPos).toLowerCase();
  }
}
