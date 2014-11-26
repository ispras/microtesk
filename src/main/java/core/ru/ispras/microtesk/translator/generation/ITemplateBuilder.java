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

package ru.ispras.microtesk.translator.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * The ITemplateBuilder interface is a base interface for all objects that are responsible for
 * initialization of class file templates.
 * 
 * @author Andrei Tatarnikov
 */

public interface ITemplateBuilder {
  /**
   * Performs initialization of the template of the target class based on templates described in the
   * corresponding template group and information extracted from the intermediate representation of
   * the target classes.
   * 
   * @param group A template group that stores templates required to build generated representation.
   * 
   * @return Fully initialized template object.
   */

  public ST build(STGroup group);
}
