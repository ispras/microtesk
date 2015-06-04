/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.test.template.Output.Argument;
import ru.ispras.microtesk.test.template.Output.ArgumentLocation;
import ru.ispras.microtesk.test.template.Output.ArgumentValue;
import ru.ispras.microtesk.utils.FormatMarker;

/**
 * The OutputBuilder class helps build Output objects.
 * 
 * @author Andrei Tatarnikov
 */

public final class OutputBuilder {
  private final boolean isRuntime;
  private final boolean isComment;
  private final String format;

  private List<Argument> args;
  private List<FormatMarker> markers;

  /**
   * Constructs an OutputBuilder object.
   * 
   * @param isRuntime Runtime status.
   * @param format Format string.
   * 
   * @throws NullPointerException if the format parameter equals null.
   */

  OutputBuilder(boolean isRuntime, boolean isComment, String format) {
    checkNotNull(format);

    this.isRuntime = isRuntime;
    this.isComment = isComment;
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

  public OutputBuilder addArgument(int value) {
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds an string format argument.
   * 
   * @param value String value.
   * @return This builder object to continue operations.
   * 
   * @throws NullPointerException if the parameter equals null.
   */

  public OutputBuilder addArgument(String value) {
    checkNotNull(value);
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
   * @throws NullPointerException if the parameter equals null.
   */

  public OutputBuilder addArgument(Value value) {
    checkNotNull(value);
    addArgument(new ArgumentValue(value));
    return this;
  }

  /**
   * Adds an boolean format argument.
   * 
   * @param value Boolean value.
   * @return This builder object to continue operations.
   */

  public OutputBuilder addArgument(boolean value) {
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
   * @throws NullPointerException if the name parameter equals null.
   */

  public OutputBuilder addArgument(String name, int index) {
    checkNotNull(name);

    final FormatMarker marker = getMarker(getArgumentCount());
    final boolean isBinaryText = FormatMarker.STR == marker || FormatMarker.BIN == marker;

    addArgument(new ArgumentLocation(name, index, isBinaryText));
    return this;
  }

  /**
   * Build the output object.
   * 
   * @return Output object.
   */

  public Output build() {
    if (null == args) {
      return new Output(isRuntime, isComment, format);
    }

    return new Output(isRuntime, isComment, format, args);
  }

  private void addArgument(Argument arg) {
    if (null == args) {
      args = new ArrayList<Output.Argument>();
    }

    args.add(arg);
  }

  private int getArgumentCount() {
    if (null == args) {
      return 0;
    }

    return args.size();
  }

  private FormatMarker getMarker(int index) {
    if (null == markers) {
      markers = FormatMarker.extractMarkers(format);
    }

    if (index >= markers.size()) {
      return null;
    }

    return markers.get(index);
  }
}
