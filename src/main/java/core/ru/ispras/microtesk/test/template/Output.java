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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.LocationAccessor;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;

/**
 * The Output class holds information to be printed to the simulation output to inserted into the
 * generated test program. The important attributes are:
 * 
 * <ol>
 * <li>Runtime. Specifies whether the information is evaluated at the simulation time and goes to
 * the simulator log or evaluated after simulation and inserted to the generated test program.</li>
 * <li>Comment. Specifies whether the printed text is a comment.</li>
 * <li>Format string. Used to describe the format of the text to be printed.</li>
 * <li>Format arguments. Pieces of information to be inserted into the printed text.</li>
 * </ol>
 * 
 * @author Andrei Tatarnikov
 */

public final class Output {
  /**
   * The Argument interface is a base interface to be implemented by all objects that store format
   * arguments.
   * 
   * @author Andrei Tatarnikov
   */

  interface Argument {
    /**
     * Evaluates the format argument using the model state observer and returns the resulting
     * object.
     * 
     * @param observer Model state observer.
     * @return Object storing the evaluation result (some data object).
     * @throws ConfigurationException if failed to evaluate the information does to an incorrect
     *         access to the model state.
     */

    Object evaluate(IModelStateObserver observer) throws ConfigurationException;
  }

  /**
   * The ArgumentValue class describes a format argument that stores a constant value (integer,
   * string or boolean). When being evaluated, just returns the stored value.
   * 
   * @author Andrei Tatarnikov
   */

  final static class ArgumentValue implements Argument {
    private final Object value;

    ArgumentValue(final Object value) {
      this.value = value;
    }

    @Override
    public Object evaluate(final IModelStateObserver observer) {
      if (value instanceof Value) {
        return ((Value) value).getValue();
      }

      return value;
    }

    @Override
    public String toString() {
      return evaluate(null).toString();
    }
  }

  /**
   * The ArgumentLocation class describes a format argument that when being evaluated reads a value
   * from the specified location (register or memory address).
   * 
   * @author Andrei Tatarnikov
   */

  final static class ArgumentLocation implements Argument {
    private final String name;
    private final BigInteger index;
    private final boolean isBinaryText;

    ArgumentLocation(final String name, final BigInteger index, final boolean isBinaryText) {
      this.name = name;
      this.index = index;
      this.isBinaryText = isBinaryText;
    }

    @Override
    public Object evaluate(final IModelStateObserver observer) throws ConfigurationException {
      final LocationAccessor accessor = observer.accessLocation(name, index);
      return isBinaryText ? accessor.toBinString() : accessor.getValue();
    }

    @Override
    public String toString() {
      return String.format("%s[%d]", name, index);
    }
  }

  private final boolean isRuntime;
  private final boolean isComment;
  private final String format;
  private final List<Argument> args;

  /**
   * Constructs an output object.
   * 
   * @param isRuntime To the simulator or to the test program.
   * @param format Format string.
   * @param args Format arguments.
   */

  public Output(
      final boolean isRuntime,
      final boolean isComment,
      final String format,
      final List<Argument> args) {
    checkNotNull(format);
    checkNotNull(args);

    this.isRuntime = isRuntime;
    this.isComment = isComment;
    this.format = format;
    this.args = args;
  }

  /**
   * Constructs an output object with no format arguments.
   * 
   * @param isRuntime To the simulator or to the test program.
   * @param format Format string.
   */

  public Output(final boolean isRuntime, final boolean isComment, final String format) {
    this(isRuntime, isComment, format, Collections.<Argument>emptyList());
  }

  /**
   * Returns {@code true} if the stored information should be evaluated during the simulation
   * and the evaluation results should be printed to the MicroTESK simulator output or
   * {@code false} if it should be evaluated after simulation and the results should be
   * inserted into the generated test program.
   * 
   * @return {@code true} if it is to printed at runtime or {@code false} if it should be
   *         printed into the test program.
   */

  public boolean isRuntime() {
    return isRuntime;
  }

  public boolean isComment() {
    return isComment;
  }

  /**
   * Evaluates the stored information using the model state observer to read the state of the model
   * (if required) and returns resulting text.
   * 
   * @param observer Model state observer.
   * @return Text to be printed.
   * @throws ConfigurationException if failed to evaluate the information due to an incorrect
   *         request to the model state observer.
   * @throws IllegalArgumentException if the parameter equals {@code null}.
   */

  public String evaluate(final IModelStateObserver observer) throws ConfigurationException {
    if (args.isEmpty()) {
      return format;
    }

    checkNotNull(observer);

    final List<Object> values = new ArrayList<Object>(args.size());
    for (Argument argument : args) {
      final Object value = argument.evaluate(observer);
      values.add(value);
    }

    return String.format(format, values.toArray());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(
       String.format("Output (runtime: %b): \"%s\"", isRuntime, format.trim()));

    for (Argument arg : args) {
      sb.append(", ");
      sb.append(arg.toString());
    }

    return sb.toString();
  }
}
