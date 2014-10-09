/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OutputBuilder.java, Aug 8, 2014 12:41:41 PM Andrei Tatarnikov
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

  OutputBuilder(boolean isRuntime, String format) {
    if (null == format) {
      throw new NullPointerException();
    }

    this.isRuntime = isRuntime;
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
    if (null == value) {
      throw new NullPointerException();
    }

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
    if (null == name) {
      throw new NullPointerException();
    }

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
      return new Output(isRuntime, format);
    }

    return new Output(isRuntime, format, args);
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
