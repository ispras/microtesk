/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.Output.Argument;
import ru.ispras.microtesk.test.template.Output.ArgumentLocation;
import ru.ispras.microtesk.test.template.Output.ArgumentValue;
import ru.ispras.microtesk.utils.FormatMarker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link OutputBuilder} class helps build {@link Output} objects.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class OutputBuilder {
  private final Output.Kind kind;
  private final String format;

  private List<Argument> args;
  private List<FormatMarker> markers;

  /**
   * Constructs an OutputBuilder object.
   * 
   * @param kindName Specifies the output type.
   * @param format Format string.
   * 
   * @throws IllegalArgumentException if the format parameter equals {@code null}.
   */
  OutputBuilder(
      final String kindName,
      final String format) {
    InvariantChecks.checkNotNull(kindName);
    InvariantChecks.checkNotNull(format);

    this.kind = Output.Kind.valueOf(kindName.toUpperCase());
    this.format = format;
    this.args = null;
    this.markers = null;
  }

  /**
   * Adds an integer format argument.
   * 
   * @param value Integer value.
   * @return This builder object to continue operations.
   */
  public OutputBuilder addArgument(final BigInteger value) {
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds an string format argument.
   * 
   * @param value String value.
   * @return This builder object to continue operations.
   * 
   * @throws IllegalAccessError if the parameter equals {@code null}.
   */
  public OutputBuilder addArgument(final String value) {
    InvariantChecks.checkNotNull(value);
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds a format argument that implements the {@link Value} interface
   * (RandomValue, UnknownValue, etc).
   * 
   * @param value Value object (implements the {@link Value} interface).
   * @return This builder object to continue operations.
   * 
   * @throws IllegalArgumentException if the parameter equals {@code null}.
   */
  public OutputBuilder addArgument(final Value value) {
    InvariantChecks.checkNotNull(value);
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds an boolean format argument.
   * 
   * @param value Boolean value.
   * @return This builder object to continue operations.
   */
  public OutputBuilder addArgument(final boolean value) {
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds a location-based format argument (will be read from the specified location at evaluation
   * time).
   * 
   * @param name Location name.
   * @param index Location index.
   * @return This builder object to continue operations.
   * 
   * @throws IllegalArgumentException if the name parameter equals {@code null}.
   */
  public OutputBuilder addArgument(final String name, final Value index) {
    InvariantChecks.checkNotNull(name);

    final FormatMarker marker = getMarker(getArgumentCount());
    final boolean isBinaryText =
        marker.isKind(FormatMarker.Kind.STR) || marker.isKind(FormatMarker.Kind.BIN);

    addArgument(new ArgumentLocation(name, index, isBinaryText));
    return this;
  }

  public OutputBuilder addArgument(final String name, final BigInteger index) {
    return addArgument(name, new FixedValue(index));
  }

  private void addArgument(final Argument arg) {
    if (null == args) {
      args = new ArrayList<>();
    }

    args.add(arg);
  }

  /**
   * Build the output object.
   * 
   * @return Output object.
   */
  public Output build() {
    return null == args ? new Output(kind, format) : new Output(kind, format, args);
  }

  private int getArgumentCount() {
    if (null == args) {
      return 0;
    }

    return args.size();
  }

  private FormatMarker getMarker(final int index) {
    if (null == markers) {
      markers = FormatMarker.extractMarkers(format);
    }

    if (index >= markers.size()) {
      return null;
    }

    return markers.get(index);
  }
}
