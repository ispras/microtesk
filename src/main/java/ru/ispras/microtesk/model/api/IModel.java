/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IModel.java, Nov 8, 2012 2:13:34 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.data.IInitializerGenerator;

/**
 * The IModel interface is main interface that should be implemented by 
 * a model. It provides method for accessing the model from the outside. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IModel
{
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
     * Returns a model state monitor object that allows getting information
     * on the current state of the microprocessor mode (current register
     * values, value in memory locations, etc)   
     */

    public IModelStateObserver getStateObserver();

    public IInitializerGenerator[] getInitializers();

    public ICallFactory getCallFactory();
}
