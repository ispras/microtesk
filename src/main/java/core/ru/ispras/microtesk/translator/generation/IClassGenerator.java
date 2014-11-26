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

import java.io.IOException;

/**
 * The IClassGenerator interface is a base interface to be implemented by all class file generators.
 * 
 * @author Andrei Tatarnikov
 */

public interface IClassGenerator {
  /**
   * Runs generation of a class file.
   * 
   * @throws Exception Raised if the generator failed to generate the needed file due to an I/O
   *         problem.
   */

  public void generate() throws IOException;
}
