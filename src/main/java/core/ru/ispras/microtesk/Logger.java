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
  public static enum EventType {
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

  public interface Listener {
    void onEventLogged(Logger.EventType type, String message);
  }

  public static final int LINE_WIDTH = 80;
  public static final String BAR = makeBar('-', LINE_WIDTH);
  public static final String SUPPORT_EMAIL = "microtesk-support@ispras.ru";

  private static boolean isDebug = false;
  private static Listener listener = null;

  public static void setDebug(final boolean value) {
    isDebug = value;
  }

  public static boolean isDebug() {
    return isDebug;
  }

  public static void setListener(final Listener value) {
    listener = value;
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

  public static void debug(final String format, final Object... args) {
    print(EventType.DEBUG, format, args);
  }

  public static void debugBar() {
    if (isDebug) {
      print(EventType.DEBUG, BAR);
    }
  }

  public static void debugHeader(final String format, final Object... args) {
    if (isDebug) {
      debugHeader(null != format ? String.format(format, args) : null);
    }
  }

  public static void debugHeader(final String text) {
    if (!isDebug) {
      return;
    }

    if (null == text || text.isEmpty()) {
      print(EventType.DEBUG, System.lineSeparator() + BAR + System.lineSeparator());
      return;
    }

    final int barLength = LINE_WIDTH - text.length();
    final int prefixLength  = barLength > 0 ? barLength / 2 : 0;
    final int postfixLength = barLength > 0 ? barLength - prefixLength : 0;

    final StringBuilder sb = new StringBuilder();

    sb.append(System.lineSeparator());
    sb.append(makeBar('-', prefixLength - 1));
    sb.append(' ');
    sb.append(text);
    sb.append(' ');
    sb.append(makeBar('-',  postfixLength - 1));
    sb.append(System.lineSeparator());

    print(EventType.DEBUG, sb.toString());
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

    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());
    sb.append("Exception stack:");
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    final java.io.StringWriter writer = new java.io.StringWriter();
    e.printStackTrace(new java.io.PrintWriter(writer));
    sb.append(writer.toString());

    sb.append(System.lineSeparator());
    sb.append(makeBar('*', LINE_WIDTH));

    print(Logger.EventType.ERROR, sb.toString());
  }

  private static void print(
      final EventType type, final String format, final Object... args) {

    if (type.isDebugOnly && !isDebug) {
      return;
    }

    if (null == args || 0 == args.length) {
      print(type, type.textPrefix + format);
      return;
    }

    print(type, type.textPrefix + (null != format ? String.format(format, args) : null));
  }

  private static void print(final EventType type, final String text) {
    System.out.println(text);

    if (null != listener) {
      listener.onEventLogged(type, text);
    }
  }

  private static String makeBar(final char ch, final int length) {
    if (length <= 0) {
      return "";
    }

    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; ++i) {
      sb.append(ch);
    }
    return sb.toString();
  }
}
