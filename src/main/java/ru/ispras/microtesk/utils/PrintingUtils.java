/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

public final class PrintingUtils {
  private PrintingUtils() {}

  public static void trace(String text) {
    System.out.println(text);
  }

  public static void trace(String format, Object... args) {
    trace(String.format(format, args));
  }

  public static void printHeader(String text) {
    final int LINE_WIDTH = 80;

    final int prefixLength = (LINE_WIDTH - text.length()) / 2;
    final int postfixLength = LINE_WIDTH - prefixLength - text.length();

    final StringBuilder sb = new StringBuilder();

    sb.append("\r\n");
    sb.append(makeLine(prefixLength - 1));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(makeLine(postfixLength - 1));
    sb.append("\r\n");

    trace(sb.toString());
  }

  public static void printHeader(String format, Object... args) {
    printHeader(String.format(format, args));
  }

  private static String makeLine(int length) {
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append('-');
    }
    return sb.toString();
  }
}
