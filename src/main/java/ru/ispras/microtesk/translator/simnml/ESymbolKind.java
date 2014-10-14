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

package ru.ispras.microtesk.translator.simnml;

/**
 * Symbols used in Sim-nML translators.
 * 
 * @author Andrei Tatarnikov
 */

public enum ESymbolKind {
  /** Reserved keywords */
  KEYWORD,

  /** Constant number or static numeric expression */
  LET_CONST,

  /** Constant label that associates some ID with a location (reg, mem or var item) */
  LET_LABEL,

  /** Constant string */
  LET_STRING,

  /** Type declaration */
  TYPE,

  /** Memory storage (reg, mem, var) */
  MEMORY,

  /** Addressing mode */
  MODE,

  /** Operation */
  OP,

  /** Argument of a mode or an operation. */
  ARGUMENT,

  /** Attribute of a mode or an operation (e.g. syntax, format, image). */
  ATTRIBUTE
}
