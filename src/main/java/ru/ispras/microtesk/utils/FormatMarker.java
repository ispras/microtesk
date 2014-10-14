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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The FormatMarker class provides facilities to identify markers within a format string. Currently,
 * the following markers are supported: %b, %d, %x and %s.
 * 
 * @author Andrei Tatarnikov
 */

public final class FormatMarker {
  /** Token for %d. Used for decimal numbers. */
  public static final FormatMarker DEC = new FormatMarker("d");

  /** Token for %b. Used for binary values (nML semantics, different from Java). */
  public static final FormatMarker BIN = new FormatMarker("b");

  /** Token for %x. Used for hexadecimal numbers. */
  public static final FormatMarker HEX = new FormatMarker("x");

  /** Token for %s. Used for string values. */
  public static final FormatMarker STR = new FormatMarker("s");

  private static final FormatMarker[] ALL_ARR = {DEC, BIN, HEX, STR};
  private static final FormatMarker ALL = new FormatMarker(ALL_ARR);

  private static final String FORMAT = "[%%][\\d]*[%s]";
  private final String tokenId;
  private final String regExpr;

  private FormatMarker(String tokenId) {
    this.tokenId = tokenId;
    this.regExpr = String.format(FORMAT, tokenId);
  }

  private FormatMarker(FormatMarker[] markers) {
    this(buildTokenId(markers));
  }

  private static String buildTokenId(FormatMarker[] markers) {
    final StringBuilder sb = new StringBuilder();
    for (FormatMarker m : markers) {
      if (0 != sb.length()) {
        sb.append('|');
      }
      sb.append(m.tokenId);
    }
    return sb.toString();
  }

  /**
   * Gets the list of format tokens for the specified format string.
   * 
   * @param format Format string to be parsed.
   * @return List of extracted tokens.
   * 
   * @throws NullPointerException if the parameter equals null.
   */

  public static List<FormatMarker> extractMarkers(String format) {
    if (null == format) {
      throw new NullPointerException();
    }

    final List<FormatMarker> result = new ArrayList<FormatMarker>();

    final Matcher matcher = Pattern.compile(ALL.regExpr).matcher(format);
    while (matcher.find()) {
      final String token = matcher.group();
      result.add(getFormatMarker(token));
    }

    return result;
  }

  private static FormatMarker getFormatMarker(String token) {
    for (FormatMarker m : ALL_ARR) {
      final Matcher matcher = Pattern.compile(m.regExpr).matcher(token);
      if (matcher.matches()) {
        return m;
      }
    }

    throw new IllegalStateException("Illegal token: " + token);
  }
}
