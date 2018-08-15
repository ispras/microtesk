/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import ru.ispras.fortress.data.types.bitvector.BitVector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class WhymlUtils {
  private WhymlUtils() {}

  public static Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
      "int", "module", "theory"
  ));

  public static String getTypeName(final int typeSize) {
    return String.format("bv%d", typeSize);
  }

  public static String getTypeFullName(final int typeSize) {
    return String.format("ispras.bv%1$d.BV%1$d", typeSize);
  }

  public static String getTypeName(final String name) {
    final String typeName = name.toLowerCase();
    return RESERVED_KEYWORDS.contains(typeName) ? typeName + "__t" : typeName;
  }

  public static String getModuleName(final String name) {
    final StringBuilder sb = new StringBuilder();
    sb.append(Character.toUpperCase(name.charAt(0)));

    if (name.length() > 1) {
      final String suffix = name.substring(1, name.length());
      sb.append(suffix.toLowerCase());
    }

    return sb.toString();
  }

  public static String getExtractTheoryName(final int sourceSize, final int fieldSize) {
    return String.format("BvExtract_%d_%d", sourceSize, fieldSize);
  }

  public static String getExtractTheoryFullName(final int sourceSize, final int fieldSize) {
    return String.format("ispras.bvextract_%1$d_%2$d.BvExtract_%1$d_%2$d", sourceSize, fieldSize);
  }

  public static String getCastTheoryName(final int sourceSize, final int targetSize) {
    return String.format("BvCast_%d_%d", sourceSize, targetSize);
  }

  public static String getCastTheoryFullName(final int sourceSize, final int targetSize) {
    return String.format("ispras.bvcast_%1$d_%2$d.BvCast_%1$d_%2$d", sourceSize, targetSize);
  }

  public static String getStateFieldName(final String name) {
    return String.format("s__.%s", name);
  }

  public static String getBitVectorText(final BitVector value) {
    final String valueText = value.toHexString();
    final int valueSize = value.getBitSize();
    return String.format("(0x%s:BV%2$d.bv%2$d)", valueText, valueSize);
  }
}
