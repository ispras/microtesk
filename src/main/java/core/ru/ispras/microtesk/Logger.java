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

public class Logger {
  private static enum EventType {
    DEBUG    (true,  "Debug: "),
    MESSAGE  (false, ""),
    WARNING  (false, "Warning: "),
    ERROR    (false, "Error: ");

    private final boolean isDebugOnly;
    private final String textPrefix;

    private EventType(boolean isDebugOnly, String textPrefix) {
      this.isDebugOnly = isDebugOnly;
      this.textPrefix = textPrefix;
    }
  }

  private static final int LINE_WIDTH = 80;
  private static final String BAR = makeBar('-', LINE_WIDTH);

  private static final String SUPPORT_EMAIL = "microtesk-support@ispras.ru";

  private static boolean isDebug = false;

  public static void setDebug(boolean value) {
    isDebug = value;
  }

  public static void debug(String format, Object... args) {
    print(EventType.DEBUG, format, args);
  }

  public static void message(String format, Object... args) {
    print(EventType.MESSAGE, format, args);
  }

  public static void warning(String format, Object... args) {
    print(EventType.WARNING, format, args);
  }

  public static void error(String format, Object... args) {
    print(EventType.ERROR, format, args);
  }

  public static void bar() {
    print(BAR);
  }

  public static void header(String format, Object... args) {
    header(null != format ? String.format(format, args) : null);
  }

  public static void header(String text) {
    if (null == text || text.isEmpty()) {
      print("\r\n" + BAR + "\r\n");
      return;
    }

    final int prefixLength = (LINE_WIDTH - text.length()) / 2;
    final int postfixLength = LINE_WIDTH - prefixLength - text.length();

    final StringBuilder sb = new StringBuilder();

    sb.append("\r\n");
    sb.append(makeBar('-', prefixLength - 1));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(makeBar('-',  postfixLength - 1));
    sb.append("\r\n");

    print(sb.toString());
  }

  public static void exception(Throwable e) {
    print(makeBar('*', LINE_WIDTH));
    print("ATTENTION! An unexpected error has occurred:");
    if (e != null) {
      print(String.format("%s: %s", e.getClass().getName(), e.getMessage()));
    } else {
      print(null);
    }
    print("\r\nThe program will be terminated. Please contact us at:");
    print(SUPPORT_EMAIL);
    print("\r\nWe are sorry for the inconvenience.");
    if (null != e) {
      print("\r\nException stack:\r\n");
      for (StackTraceElement ste : e.getStackTrace()) {
        print(ste.toString());
      }
    }
    print(makeBar('*', LINE_WIDTH));
  }

  private static void print(EventType type, String format, Object... args) {
    if (type.isDebugOnly && !isDebug) {
      return;
    }

    if (null == args || 0 == args.length) {
      print(type.textPrefix + format);
      return;
    }

    print(type.textPrefix + (null != format ? String.format(format, args) : null)); 
  }

  private static void print(String text) {
    System.out.println(text);
  }

  private static String makeBar(char ch, int length) {
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append(ch);
    }
    return sb.toString();
  }
}
