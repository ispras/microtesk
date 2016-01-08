/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link AddressingModeBuilder} class implements logic responsible for
 * creating and initializing addressing mode objects.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AddressingModeBuilder extends PrimitiveBuilder<IAddressingMode>{
  /**
   * Creates a builder for an addressing mode based on the specified configuration parameters.
   * 
   * @param modeName The name of the mode.
   * @param factory The factory for creating the specified mode.
   * @param decls The table of mode argument declarations.
   */

  public AddressingModeBuilder(
      final String modeName,
      final IPrimitive.IFactory<IAddressingMode> factory,
      final ArgumentDecls decls) {
    super(
        modeName,
        factory,
        decls
        );
  }
}
