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

package ru.ispras.microtesk.model.api.tarmac;

/**
 * The {@code RecordKind} enumeration describes types or log records used
 * in the Tarmac trace format. See documentation on the Tarmac format for details.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum RecordKind {
  /**
   * Instruction trace:<p>
   * {@code <time> <scale> <cpu> [IT|IS] (<inst_id>) <addr> <opcode> [A|T|X] <mode>_<security> :
   * <disasm>}
   */
  INSTRUCT, 

  /**
   * Program flow trace:<p>
   * {@code <time> <scale> [FD|FI|FR] (<inst_id>) <addr> <targ_addr> [A|T|X]}
   */
  FLOW,

  /**
   * Register trace:<p>
   * {@code <time> <scale> R <register> <value>}
   */
  REGISTER,

  /**
   * Event trace:<p>
   * {@code <time> <scale> E <value> <number> <desc>}
   */
  EVENT,

  /**
   * Processor memory access trace:<p>
   * {@code <time> <scale> M<rw><sz><attrib> <addr> <data>}
   */
  MEMORY,

 /**
 * Memory bus trace:<p>
 * {@code <time> <scale> B<rw><sz><fd><lk><p><s> l<wrcbs> O<wrcbs> <master_id> <addr> <data>}
 */
  BUS
}
