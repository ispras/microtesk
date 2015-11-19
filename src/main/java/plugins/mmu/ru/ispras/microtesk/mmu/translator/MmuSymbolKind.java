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

package ru.ispras.microtesk.mmu.translator;

public enum MmuSymbolKind {
  /** External value (read for a register or an addressing mode) */
  EXTERN,

  /** Reserved keywords */
  KEYWORD,

  /** Address */
  ADDRESS,

  /** Segment */
  SEGMENT,

  /** Buffer */
  BUFFER,

  /** Memory logic (MMU) */
  MEMORY,

  /** Address argument (used by segment, buffer and memory entities) */
  ARGUMENT,

  /** Entry field (included in Buffer.Entry) */
  FIELD,

  /** Data argument (used by memory entities) */
  DATA,

  /** Local variable (used in attributes of memory entities) */
  VAR,

  /** Attribute (used in memory entities to describe actions) */
  ATTRIBUTE,

  TYPE,
  FUNCTION,

  /** Operation associated with an operation in the ISA specification */
  OPERATION
}
