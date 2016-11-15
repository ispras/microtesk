/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * The {@link FormatMarker} class provides facilities to identify markers within a format string.
 * Currently, the following markers are supported: %b, %d, %x and %s.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class FormatMarker {
  /**
   * The {@link Kind} enumeration describes supported types of format markers.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static enum Kind {
    /** Marker %d. Used for decimal numbers. */
    DEC('d', false),

    /** Marker %b. Used for binary values (nML semantics, different from Java). */
    BIN('b', false),

    /** Marker %x. Used for hexadecimal numbers. */
    HEX('x', true),

    /** Marker %s. Used for string values. When applied to locations, works as {@link Kind#BIN}. */
    STR('s', true);

    private static final Map<Character, Kind> INSTANCES = new HashMap<>();

    private static final String REG_EXPR_FORMAT = "[%%][\\d]*[%s]";
    public static final String REG_EXPR;

    private final char letter;
    private final boolean hasUpperCase;
    private final String regExpr;

    private Kind(final char letter, final boolean hasUpperCase) {
      this.letter = Character.toLowerCase(letter);
      this.hasUpperCase = hasUpperCase;

      final StringBuilder sb = new StringBuilder();
      sb.append(letter);

      if (hasUpperCase) {
        sb.append('|');
        sb.append(Character.toUpperCase(letter));
      }

      this.regExpr = String.format(REG_EXPR_FORMAT, sb.toString());
    }

    public char getLetter() {
      return letter;
    }

    static {
      for (final Kind kind : values()) {
        INSTANCES.put(kind.letter, kind);
        if (kind.hasUpperCase) {
          INSTANCES.put(Character.toUpperCase(kind.letter), kind);
        }
      }

      REG_EXPR = String.format(
          REG_EXPR_FORMAT, StringUtils.toString(INSTANCES.keySet(), "|"));
    }

    /**
     * Returns the marker kind that correspond to the specified letter.
     * 
     * @param letter Marker letter.
     * @return Marker kind for the letter or {@code null} if there no such marker is supported.
     */
    public static Kind fromLetter(final char letter) {
      return INSTANCES.get(letter);
    }
  }

  private final Kind kind;

  private FormatMarker(final Kind kind) {
    this.kind = kind;
  }

  public Kind getKind() {
    return kind;
  }

  public boolean isKind(final Kind kind) {
    return this.kind == kind;
  }

  /**
   * Returns the token identifier for the given marker type.
   * 
   * @return Token identifier.
   */
  public String getTokenId() {
    return String.valueOf(kind.letter);
  }

  /**
   * Gets the regular expression describing the given marker type.
   * 
   * @return Regular expression.
   */
  public String getRegExpr() {
    return kind.regExpr;
  }

  /**
   * Gets the list of format tokens for the specified format string.
   * 
   * @param format Format string to be parsed.
   * @return List of extracted tokens.
   * 
   * @throws IllegalArgumentException if the parameter is {@code null}.
   */
  public static List<FormatMarker> extractMarkers(final String format) {
    InvariantChecks.checkNotNull(format);

    final List<FormatMarker> result = new ArrayList<>();

    final Matcher matcher = Pattern.compile(Kind.REG_EXPR).matcher(format);
    while (matcher.find()) {
      final String token = matcher.group();
      result.add(getFormatMarker(token));
    }

    return result;
  }

  private static FormatMarker getFormatMarker(final String token) {
    for (final Kind kind : Kind.values()) {
      final Matcher matcher = Pattern.compile(kind.regExpr).matcher(token);
      if (matcher.matches()) {
        return new FormatMarker(kind);
      }
    }

    throw new IllegalStateException("Illegal token: " + token);
  }
}
