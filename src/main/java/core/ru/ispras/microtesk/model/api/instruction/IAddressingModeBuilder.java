/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.instruction;

import java.math.BigInteger;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IAddressingModeBuilder interface allows providing an addressing mode object that is
 * represents an argument of the specified instruction with necessary argument values.
 * 
 * @author Andrei Tatarnikov
 */

public interface IAddressingModeBuilder {
  /**
   * Initializes the specified addressing mode argument with an integer value.
   * 
   * @param name Mode argument name.
   * @param value Mode argument integer value.
   */

  IAddressingModeBuilder setArgumentValue(String name, BigInteger value)
      throws ConfigurationException;

  /**
   * Returns an addressing mode object created by the builder.
   * 
   * @return The addressing mode object.
   * @throws ConfigurationException Exception that informs of an error that occurs on attempt to
   *         build an addressing mode object due to incorrect configuration.
   */

  IAddressingMode build() throws ConfigurationException;
}
