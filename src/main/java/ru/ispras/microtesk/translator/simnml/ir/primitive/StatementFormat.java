/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;
import ru.ispras.microtesk.utils.FormatMarker;

public final class StatementFormat extends Statement {
  private final String format;
  private final List<FormatMarker> markers;
  private final List<Format.Argument> arguments;

  StatementFormat(String format, List<FormatMarker> markers, List<Format.Argument> arguments) {
    super(Kind.FORMAT);

    this.format = format;
    this.markers = markers;
    this.arguments = arguments;
  }

  public String getFormat() {
    return format;
  }

  public List<FormatMarker> getMarkers() {
    return markers;
  }

  public List<Format.Argument> getArguments() {
    return arguments;
  }
}
