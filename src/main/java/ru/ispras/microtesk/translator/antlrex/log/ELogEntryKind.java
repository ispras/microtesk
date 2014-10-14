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

package ru.ispras.microtesk.translator.antlrex.log;

/**
 * The ELogEntryKind enumeration describes three levels of an event or an exception (usually a
 * record is added to the log due to a runtime exception) that can occur during translation of an
 * ADL specification.
 * 
 * @author Andrei Tatarnikov
 */

public enum ELogEntryKind {
  /**
   * Signifies a severe translation error. Usually it means that some the design specification was
   * incorrect, which cause translation to fail. In other words, the tool was unable to produce any
   * meaningful output.
   */
  ERROR,

  /**
   * Signifies a minor translation error. This usually means a small issue in the design
   * specification which can potentially cause incorrect results.
   */
  WARNING,

  /**
   * Signifies an informational message that highlights some issue in the ADL specification (or in
   * the tool) that requires user attention, but is not necessarily an error.
   */
  MESSAGE
}
