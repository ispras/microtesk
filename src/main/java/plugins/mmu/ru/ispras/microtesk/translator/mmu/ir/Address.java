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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class Address {
  private final String name;
  private final int width;
  private final Format format;
  private final Map<String, Segment> segments;

  Address(String name, int width, Format format, Map<String, Segment> segments) {
    checkNotNull(name);
    checkGreaterThanZero(width);
    checkNotNull(format);
    checkNotNull(segments);

    this.name = name;
    this.width = width;
    this.format = format;
    this.segments = segments;
  }

  public String getName() {
    return name;
  }

  public int getWidth() {
    return width;
  }

  public Format getFormat() {
    return format;
  }

  public Collection<Segment> getSegments() {
    return Collections.unmodifiableCollection(segments.values());
  }

  public Segment getSegment(String name) {
    checkNotNull(name);
    return segments.get(name);
  }
}
