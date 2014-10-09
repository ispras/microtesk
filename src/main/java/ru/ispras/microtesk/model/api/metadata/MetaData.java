/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaDataItem.java, Aug 7, 2014 4:39:13 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

/**
 * The MetaData interface is a common interface to be implemented by metadata object. It is needed
 * to work with collections of different metadata objects in a uniform way.
 * 
 * @author Andrei Tatarnikov
 */

interface MetaData {
  /**
   * Returns the name of the current metadata element.
   * 
   * @return Metadata element name.
   */

  String getName();
}
