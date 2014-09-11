/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TypeId.java, Oct 8, 2012 12:21:36 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.type;

/**
 * The TypeId enumeration stores the list of data types (ways to interpret
 * raw data) supported by the model. The data types are taken from
 * the Sim-nML language. 
 *
 * @author Andrei Tatarnikov
 */

public enum TypeId
{
    INT,
    CARD,
    FLOAT,
    FIX,
    RANGE,
    ENUM,
    BOOL
}
