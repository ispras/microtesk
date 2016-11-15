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
    private static final String REG_EXPR;

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

    /**
     * Returns the letter identifying the marker.
     * 
     * @return Marker letter.
     */
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

  private static final Pattern MARKER_PATTERN = Pattern.compile(Kind.REG_EXPR);
  private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d+");

  private final Kind kind;
  private final int length;

  private final int start;
  private final int end;

  private FormatMarker(
      final Kind kind,
      final int length,
      final int start,
      final int end) {
    this.kind = kind;
    this.length = length;
    this.start = start;
    this.end = end;
  }

  /**
   * Returns the marker kind.
   * 
   * @return Marker kind.
   */
  public Kind getKind() {
    return kind;
  }

  /**
   * Check whether the marker of the specified kind.
   * 
   * @param kind Marker kind.
   * @return {@code true} if the marker is of the specified kind or {@code false} otherwise.
   */
  public boolean isKind(final Kind kind) {
    return this.kind == kind;
  }

  /**
   * Returns the length specified in the marker.
   * 
   * @return Length specified in the marker or {@code 0} if no length is specified.
   */
  public int getLength() {
    return length;
  }

  /**
   * Returns the start position of the marker (first character of the marker)
   * in the format string.
   * 
   * @return Start position of the marker in the format string.
   */
  public int getStart() {
    return start;
  }

  /**
   * Returns the end position of the marker (first character after the marker)
   * in the format string.
   * 
   * @return End position of the marker in the format string.
   */
  public int getEnd() {
    return end;
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

    final Matcher matcher = MARKER_PATTERN.matcher(format);
    while (matcher.find()) {
      final String token = matcher.group();
      final FormatMarker marker = newFormatMarker(token, matcher.start(), matcher.end());
      result.add(marker);
    }

    return result;
  }

  private static FormatMarker newFormatMarker(
      final String token,
      final int start,
      final int end) {
    final char letter = token.charAt(token.length() - 1);
    final Kind kind = Kind.fromLetter(letter);

    if (null == kind) {
      throw new IllegalArgumentException("Illegal token: " + token);
    }

    final int length;
    final Matcher matcher = INTEGER_PATTERN.matcher(token);

    if (matcher.find()) {
      length = Integer.parseInt(matcher.group());
    } else {
      length = 0;
    }

    return new FormatMarker(kind, length, start, end);
  }

  @Override
  public String toString() {
    return String.format("%s:%d[%d, %d)", kind, length, start, end);
  }
}
