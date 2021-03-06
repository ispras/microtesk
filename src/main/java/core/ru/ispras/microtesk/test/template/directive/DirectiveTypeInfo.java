/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.utils.FormatMarker;

import java.util.List;

public final class DirectiveTypeInfo {
  public final Type type;
  public final String text;
  public final String format;
  public final boolean align;
  public final FormatMarker formatMarker;

  DirectiveTypeInfo(
      final Type type,
      final String text,
      final String format,
      final boolean align) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(format);

    this.type = type;
    this.text = text;
    this.format = format;
    this.align = align;

    if (format.isEmpty()) {
      this.formatMarker = null;
    } else {
      final List<FormatMarker> formatMarkers = FormatMarker.extractMarkers(format);
      InvariantChecks.checkTrue(formatMarkers.size() == 1,
          "For a format string a single format marker is required.");
      this.formatMarker = formatMarkers.get(0);
    }
  }
}
