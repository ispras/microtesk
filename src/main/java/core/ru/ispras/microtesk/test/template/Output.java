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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.memory.LocationAccessor;
import ru.ispras.microtesk.model.api.ModelStateObserver;
import ru.ispras.microtesk.utils.SharedObject;

/**
 * The {@link Output} class holds information to be printed to the simulation output to inserted
 * into the generated test program. The important attributes are:
 * 
 * <ol>
 * <li>Runtime. Specifies whether the information is evaluated at the simulation time and goes to
 * the simulator log or evaluated after simulation and inserted to the generated test program.</li>
 * <li>Comment. Specifies whether the printed text is a comment.</li>
 * <li>Format string. Used to describe the format of the text to be printed.</li>
 * <li>Format arguments. Pieces of information to be inserted into the printed text.</li>
 * </ol>
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Output {
  /**
   * The {@link Kind} enum describes the type of the output.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public static enum Kind {
    /** Text message */
    TEXT(false, false),

    /** Message to the simulator log*/
    TRACE(true, false),

    /** Single-line comment */
    COMMENT(false, true),

    /** In-line comment */
    COMMENT_INLINE(false, true),

    /** Start of a multiline comment */
    COMMENT_ML_START(false, true),

    /** Body of a multiline comment */
    COMMENT_ML_BODY(false, true),

    /** End of a multiline comment */
    COMMENT_ML_END(false, true);

    private final boolean isRuntime;
    private final boolean isComment;

    private Kind(final boolean isRuntime, final boolean isComment) {
      this.isRuntime = isRuntime;
      this.isComment = isComment;
    }
  }

  /**
   * The Argument interface is a base interface to be implemented by all objects that store format
   * arguments.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
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
    Object evaluate(ModelStateObserver observer) throws ConfigurationException;

    /**
     * Creates a copy.
     * 
     * @return A new copy.
     */
    Argument copy();
  }

  /**
   * The ArgumentValue class describes a format argument that stores a constant value (integer,
   * string or boolean). When being evaluated, just returns the stored value.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  final static class ArgumentValue implements Argument {
    private final Object value;

    ArgumentValue(final Object value) {
      this.value = value;
    }

    @Override
    public Object evaluate(final ModelStateObserver observer) {
      if (value instanceof Value) {
        return ((Value) value).getValue();
      }

      return value;
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public Argument copy() {
      return value instanceof SharedObject ?
          new ArgumentValue(((SharedObject<?>) value).getCopy()) : this;
    }
  }

  /**
   * The ArgumentLocation class describes a format argument that when being evaluated reads a value
   * from the specified location (register or memory address).
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
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
    public Object evaluate(final ModelStateObserver observer) throws ConfigurationException {
      final LocationAccessor accessor = observer.accessLocation(name, index);
      return isBinaryText ? accessor.toBinString() : accessor.getValue();
    }

    @Override
    public String toString() {
      return String.format("%s[%d]", name, index);
    }

    @Override
    public Argument copy() {
      return this;
    }
  }

  private final Kind kind;
  private final String format;
  private final List<Argument> args;

  /**
   * Constructs an output object.
   * 
   * @param kind Output type.
   * @param format Format string.
   * @param args Format arguments.
   */
  public Output(
      final Kind kind,
      final String format,
      final List<Argument> args) {
    checkNotNull(kind);
    checkNotNull(format);
    checkNotNull(args);

    this.kind = kind;
    this.format = format;
    this.args = args;
  }

  /**
   * Constructs an output object with no format arguments.
   * 
   * @param kind Output type.
   * @param format Format string.
   */
  public Output(final Kind kind, final String format) {
    this(kind, format, Collections.<Argument>emptyList());
  }

  /**
   * Constructs a copy of an {@link Output} object.
   * 
   * @param other Object to be copied.
   */
  public Output(final Output other) {
    InvariantChecks.checkNotNull(other);

    this.kind = other.kind;
    this.format = other.format;
    this.args = copyAllArguments(other.args);
  }

  /**
   * Returns the output kind.
   * 
   * @return Output kind.
   */
  public Kind getKind() {
    return kind;
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
    return kind.isRuntime;
  }

  public boolean isComment() {
    return kind.isComment;
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
  public String evaluate(final ModelStateObserver observer) throws ConfigurationException {
    if (args.isEmpty()) {
      return format;
    }

    checkNotNull(observer);

    final List<Object> values = new ArrayList<>(args.size());
    for (final Argument argument : args) {
      final Object value = argument.evaluate(observer);
      values.add(value);
    }

    return String.format(format, values.toArray());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(
        String.format("Output (kind: %s): \"%s\"", kind, format.trim()));

    for (final Argument arg : args) {
      sb.append(", ");
      sb.append(arg.toString());
    }

    return sb.toString();
  }

  public static List<Output> copyAll(final List<Output> outputs) {
    InvariantChecks.checkNotNull(outputs);

    if (outputs.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Output> result = new ArrayList<>(outputs.size());
    for (final Output output : outputs) {
      result.add(new Output(output));
    }

    return result;
  }

  private static List<Argument> copyAllArguments(final List<Argument> arguments) {
    InvariantChecks.checkNotNull(arguments);

    if (arguments.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Argument> result = new ArrayList<>(arguments.size());
    for (final Argument argument : arguments) {
      result.add(argument.copy());
    }

    return result;
  }
}
