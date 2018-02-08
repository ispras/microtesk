/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link Mask} class implements masks applied to bit vectors.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Mask {
  // First: mask text, second: true for binary format, false for hexadecimal.
  private final List<Pair<String, Boolean>> masks;

  private Mask(final List<Pair<String, Boolean>> masks) {
    InvariantChecks.checkNotEmpty(masks);
    this.masks = masks;
  }

  public boolean isMatch(final BitVector value) {
    InvariantChecks.checkNotNull(value);

    String binText = null;
    String hexText = null;

    for (final Pair<String, Boolean> mask: masks) {
      final String maskText = mask.first;
      final boolean isBinary = mask.second;

      if (isBinary) {
        if (null == binText) {
          binText = value.toBinString();
        }
      } else {
        if (null == hexText) {
          final int hexLenght = value.getBitSize() / 4 + value.getBitSize() % 4;
          hexText = value.toHexString();

          // Need all hexadecimal digits including trailing zeros.
          if (hexLenght != hexText.length()) {
            hexText = String.format("%0" + (hexLenght - hexText.length()) + "d%s", 0, hexText);
          }
        }
      }

      if (testMask(maskText, isBinary ? binText : hexText)) {
        return true;
      }
    }

    return false;
  }

  private static boolean testMask(final String mask, final String value) {
    if (mask.length() != value.length()) {
      return false;
    }

    final int length = mask.length();
    for (int index = 0; index < length; ++index) {
      final char  maskCh =  mask.charAt(index);
      final char valueCh = value.charAt(index);

      if (maskCh != valueCh && maskCh != 'X') {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final Mask other = (Mask) obj;
    return masks.equals(other.masks);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    final boolean isSingle = masks.size() == 1;

    if (!isSingle) {
      sb.append('[');
    }

    boolean isFirst = true;
    for (final Pair<String, Boolean> mask : masks) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(", ");
      }

      sb.append(mask.first.length() * (mask.second ? 1 : 4));
      sb.append(mask.second ? 'b' : 'h');
      sb.append('\'');
      sb.append(mask.first);
      sb.append('\'');
    }

    if (!isSingle) {
      sb.append(']');
    }

    return sb.toString();
  }

  public static Mask valueOf(final String maskText) {
    InvariantChecks.checkNotNull(maskText);

    final Pair<String, Boolean> mask = makeMask(maskText);
    if (null == mask) {
      return null;
    }

    return new Mask(Collections.singletonList(mask));
  }

  public static Mask valueOf(final Collection<String> maskTexts) {
    InvariantChecks.checkNotEmpty(maskTexts);

    final List<Pair<String, Boolean>> masks = new ArrayList<>(maskTexts.size());
    for (final String maskText : maskTexts) {
      final Pair<String, Boolean> mask = makeMask(maskText);
      if (null == mask) {
        return null;
      }

      masks.add(mask);
    }
    return new Mask(masks);
  }

  private static final String HEX_RE = "[0-9A-FX]+";
  private static final String BIN_RE = "[01X]+";
  private static final String MASK_RE = String.format("^((\\d*)(\'([B|H])))?(%s)", HEX_RE);

  private static final Pattern BIN_PTRN = Pattern.compile(BIN_RE);
  private static final Pattern MASK_PTRN = Pattern.compile(MASK_RE);

  private static Pair<String, Boolean> makeMask(final String text) {
    InvariantChecks.checkNotNull(text);
    final String textUpperCaseNoUndercope =
        text.replaceAll("_", "").toUpperCase();

    final Matcher matcher = MASK_PTRN.matcher(textUpperCaseNoUndercope);
    if (!matcher.matches()) {
      return null;
    }

    final String sizeText = matcher.group(2);
    final String typeText = matcher.group(4);
    final String maskText = matcher.group(5);

    final boolean isBinary = "B".equals(typeText);
    if (isBinary) {
      final Matcher binMatcher = BIN_PTRN.matcher(maskText);
      if (!binMatcher.matches()) {
        return null;
      }
    }

    if (null != sizeText && !sizeText.isEmpty()) {
      final int size = Integer.parseInt(sizeText);
      final int maskSize = maskText.length() * (isBinary ? 1 : 4);

      if (size != maskSize) {
        return null;
      }
    }

    return new Pair<>(maskText, isBinary);
  }
}
