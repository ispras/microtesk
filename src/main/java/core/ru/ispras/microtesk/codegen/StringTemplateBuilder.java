/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.codegen;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * The {@link StringTemplateBuilder} interface is a base interface for all objects
 * that are responsible for initialization of StringTemplate objects.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public interface StringTemplateBuilder {
  /**
   * Performs initialization of the template of the target file based on templates
   * described in the corresponding template group.
   *
   * @param group A template group that stores templates required to accomplish code generation.
   *
   * @return Fully initialized StringTemplate ({@link ST}) object.
   */
  ST build(STGroup group);
}
