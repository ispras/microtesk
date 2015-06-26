/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.antlrex;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.antlr.runtime.ANTLRFileStream;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IncludeFileFinder {
  private List<String> dirs = new LinkedList<String>();

  public void addPath(final String path) {
    dirs.add(path);
  }

  public void addPaths(final String paths) {
    final StringTokenizer tokenizer = new StringTokenizer(paths, File.pathSeparator);

    while (tokenizer.hasMoreTokens()) {
      final String path = tokenizer.nextToken();
      addPath(path);
    }
  }

  public ANTLRFileStream openFile(final String filename) {
    File file = new File(filename);

    if (!file.exists() && !file.isAbsolute()) {
      for (final String path : dirs) {
        final String filepath = path + File.separator + filename;
        if ((file = new File(filepath)).exists()) {
          break;
        }
      }
    }

    try {
      return file.exists() ? new ANTLRFileStream(file.getAbsolutePath()) : null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
