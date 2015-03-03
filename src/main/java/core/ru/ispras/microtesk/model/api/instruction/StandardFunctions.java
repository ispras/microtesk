/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

public abstract class StandardFunctions {
  public static void exception(String text) {
    System.out.println("Exception was raised: " + text);
  }

  public static void trace(String format, Object ... args) {
    System.out.println(String.format(format, args));
  }

  public static void unpredicted() {
    System.out.println("Unpredicted state was reached");
  }

  public static void undefined() {
    System.out.println("Undefined state was reached");
  }

  public static void mark(String name) {
    //System.out.println(String.format("Mark \"%s\" was reached", name));
  }
}
