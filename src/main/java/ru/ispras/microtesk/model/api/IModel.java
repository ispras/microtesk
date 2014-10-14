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

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;

/**
 * The IModel interface is main interface that should be implemented by a model. It provides method
 * for accessing the model from the outside.
 * 
 * @author Andrei Tatarnikov
 */

public interface IModel {
  /**
   * Returns the name of the modeled microprocessor design.
   * 
   * @return name Microprocessor design name.
   */

  public String getName();

  /**
   * Returns a meta description of the model.
   * 
   * @return An meta data object (provides access to model's meta data).
   */

  public MetaModel getMetaData();

  /**
   * Returns a model state monitor object that allows getting information on the current state of
   * the microprocessor mode (current register values, value in memory locations, etc)
   */

  public IModelStateObserver getStateObserver();

  /**
   * Returns a factory for creating instances of operations, addressing modes and instruction calls
   * that can be simulated with the model.
   * 
   * @return Call factory.
   */

  public ICallFactory getCallFactory();
}
