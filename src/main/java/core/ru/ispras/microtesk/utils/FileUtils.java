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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.ispras.fortress.util.InvariantChecks;

public final class FileUtils {
  private FileUtils() {}

  public static String getNormalizedPath(final String filePath) {
    InvariantChecks.checkNotNull(filePath);

    if ('/' != File.separatorChar) {
      return filePath.replace('/', File.separatorChar);
    }
    if ('\\' != File.separatorChar) {
      return filePath.replace('\\', File.separatorChar);
    }

    return filePath;
  }

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

  public static String getFileDir(final String fileName) {
    InvariantChecks.checkNotNull(fileName);

    final File file = new File(fileName);
    return file.getParent();
  }

  public static void copy(final File sourceLocation, final File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      copyDirectory(sourceLocation, targetLocation);
    } else {
      copyFile(sourceLocation, targetLocation);
    }
  }

  public static void copyDirectory(File source, File target) throws IOException {
    if (!target.exists()) {
      target.mkdir();
    }

    for (final String f : source.list()) {
      copy(new File(source, f), new File(target, f));
    }
  }

  public static void copyFile(final File source, final File target) throws IOException {
    try (final InputStream in = new FileInputStream(source);
        final OutputStream out = new FileOutputStream(target)) {
      byte[] buf = new byte[1024];
      int length;
      while ((length = in.read(buf)) > 0) {
        out.write(buf, 0, length);
      }
    }
  }
}
