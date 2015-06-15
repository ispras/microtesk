/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;

/**
 * The Value interface is to be implemented by all classes that hold immediate values (arguments
 * of addressing modes and operations). This provides a uniform way to extract the stored
 * (or generated on the fly) immediate values.
 * 
 * @author Andrei Tatarnikov
 */

public interface Value {
  BigInteger getValue();
}
