/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

public interface IOperationBuilder {
  IOperationBuilder setArgument(String name, String value) throws ConfigurationException;
  IOperationBuilder setArgument(String name, BigInteger value) throws ConfigurationException;

  IOperationBuilder setArgument(String name, IAddressingMode value)
      throws ConfigurationException;

  IOperationBuilder setArgument(String name, IOperation value)
      throws ConfigurationException;

  IOperation build() throws ConfigurationException;
}
