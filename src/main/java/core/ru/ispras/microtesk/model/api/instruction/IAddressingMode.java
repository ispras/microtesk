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

import java.util.Collection;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;

/**
 * The IAddressingMode interface is the base interface for addressing mode objects or OR rules that
 * group addressing mode objects. It extends the IInstructionPrimitive interface allowing to access
 * the location the mode object points to and to trace attempts to access the location. Also, it
 * includes enclosed interfaces that are base interfaces for different parts of addressing mode
 * objects.
 * 
 * @author Andrei Tatarnikov
 */

public interface IAddressingMode extends IPrimitive {

  /**
   * Returns the location the addressing mode object points to (when initialized with specific
   * parameters).
   * 
   * @return The memory location.
   */

  Location access();

  /**
   * The IInfo interface provides information on an addressing mode object or a group of addressing
   * mode object united by an OR rule. This information is needed to instantiate a concrete
   * addressing mode object at runtime depending on the selected builder.
   * 
   * @author Andrei Tatarnikov
   */

  public interface IInfo {
    /**
     * Returns the name of the mode or the name of the OR rule used for grouping addressing modes.
     * 
     * @return The mode name.
     */

    String getName();

    /**
     * Returns the type of data accessed via the addressing mode.
     * 
     * @return Data type.
     */

    Type getType();

    /**
     * Returns a table of builder for the addressing mode (or the group of addressing modes)
     * described by the current info object.
     * 
     * @return A table of addressing mode builders (key is the mode name, value is the builder).
     */

    Map<String, AddressingModeBuilder> createBuilders();

    /**
     * Returns a collection of meta data objects describing the addressing mode (or the group of
     * addressing modes) the info object refers to. In the case, when there is a single addressing
     * mode, the collection will contain only one item.
     * 
     * @return A collection of meta data objects for an addressing mode or a group of addressing
     *         modes.
     */

    Collection<MetaAddressingMode> getMetaData();

    /**
     * Checks if the current addressing mode (or group of addressing modes) implements (or contains)
     * the specified addressing mode. This method is used in runtime checks to make sure that the
     * object composition in the model is valid.
     * 
     * @param mode An addressing mode object.
     * @return true if the mode is supported or false otherwise.
     */

    boolean isSupported(IAddressingMode mode);
  }
}
