/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.AddressingMode;

/**
 * The {@link IsaAddressingMode} class provides information on an addressing
 * mode that is used to access a location.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class IsaAddressingMode implements AddressingMode {
  private final IsaPrimitive addressingMode;
  private final TemporaryVariables temporaryVariables;
  private Map<String, BigInteger> arguments;

  protected IsaAddressingMode(
      final IsaPrimitive addressingMode,
      final TemporaryVariables temporaryVariables) {
    InvariantChecks.checkNotNull(addressingMode);
    InvariantChecks.checkNotNull(temporaryVariables);

    this.addressingMode = addressingMode;
    this.temporaryVariables = temporaryVariables;
    this.arguments = null;
  }

  @Override
  public String getName() {
    return addressingMode.getName();
  }

  @Override
  public String getSyntax() {
    return addressingMode.syntax(temporaryVariables);
  }

  @Override
  public Map<String, BigInteger> getArguments() {
    if (null == arguments) {
      arguments = makeArguments(addressingMode);
    }

    return arguments;
  }

  private static Map<String, BigInteger> makeArguments(final IsaPrimitive primitive) {
    final Map<String, BigInteger> result = new LinkedHashMap<>();

    for (final Map.Entry<String, IsaPrimitive> entry : primitive.getArguments().entrySet()) {
      final String name = entry.getKey();
      final IsaPrimitive argument = entry.getValue();

      InvariantChecks.checkTrue(argument instanceof Immediate);
      final BigInteger value = ((Immediate) argument).access().getValue();

      result.put(name, value);
    }

    return Collections.unmodifiableMap(result);
  }
}
