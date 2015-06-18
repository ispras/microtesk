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

package ru.ispras.microtesk;

public final class Logger {
  private static enum EventType {
    DEBUG    (true,  ""),
    MESSAGE  (false, ""),
    WARNING  (false, "Warning: "),
    ERROR    (false, "Error: ");

    private final boolean isDebugOnly;
    private final String textPrefix;

    private EventType(final boolean isDebugOnly, final String textPrefix) {
      this.isDebugOnly = isDebugOnly;
      this.textPrefix = textPrefix;
    }
  }

  public static final int LINE_WIDTH = 80;
  public static final String BAR = makeBar('-', LINE_WIDTH);
  public static final String SUPPORT_EMAIL = "microtesk-support@ispras.ru";

  private static boolean isDebug = false;

  public static void setDebug(final boolean value) {
    isDebug = value;
  }

  public static void debug(final String format, final Object... args) {
    print(EventType.DEBUG, format, args);
  }

  public static void message(final String format, final Object... args) {
    print(EventType.MESSAGE, format, args);
  }

  public static void warning(final String format, final Object... args) {
    print(EventType.WARNING, format, args);
  }

  public static void error(final String format, final Object... args) {
    print(EventType.ERROR, format, args);
  }

  public static void bar() {
    print(BAR);
  }

  public static void header(final String format, final Object... args) {
    header(null != format ? String.format(format, args) : null);
  }

  public static void header(final String text) {
    if (null == text || text.isEmpty()) {
      print(System.lineSeparator() + BAR + System.lineSeparator());
      return;
    }

    final int prefixLength = (LINE_WIDTH - text.length()) / 2;
    final int postfixLength = LINE_WIDTH - prefixLength - text.length();

    final StringBuilder sb = new StringBuilder();

    sb.append(System.lineSeparator());
    sb.append(makeBar('-', prefixLength - 1));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(makeBar('-',  postfixLength - 1));
    sb.append(System.lineSeparator());

    print(sb.toString());
  }

  public static void exception(final Throwable e) {
    final StringBuilder sb = new StringBuilder();

    sb.append(makeBar('*', LINE_WIDTH));
    sb.append(System.lineSeparator());
    sb.append("ATTENTION! An unexpected error has occurred:");
    sb.append(System.lineSeparator());

    if (e != null) {
      sb.append(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
    }
    sb.append(System.lineSeparator());

    sb.append(System.lineSeparator());
    sb.append("The program will be terminated. Please contact us at: ");
    sb.append(System.lineSeparator());
    sb.append(SUPPORT_EMAIL);

    sb.append(System.lineSeparator());
    sb.append("We are sorry for the inconvenience.");

    print(sb.toString());

    if (null != e) {
      print("\r\nException stack:\r\n");
      e.printStackTrace(System.out);
    }
    print(makeBar('*', LINE_WIDTH));
  }

  private static void print(
      final EventType type, final String format, final Object... args) {

    if (type.isDebugOnly && !isDebug) {
      return;
    }

    if (null == args || 0 == args.length) {
      print(type.textPrefix + format);
      return;
    }

    print(type.textPrefix + (null != format ? String.format(format, args) : null)); 
  }

  private static void print(final String text) {
    System.out.println(text);
  }

  private static String makeBar(final char ch, final int length) {
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append(ch);
    }
    return sb.toString();
  }
}
