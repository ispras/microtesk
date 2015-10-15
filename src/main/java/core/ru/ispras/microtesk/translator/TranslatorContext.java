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

package ru.ispras.microtesk.translator;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

public final class TranslatorContext {
  // Internal representations of different specifications (describing different features
  // of the modeled processor), produced by different translators. Filled Incrementally.
  private final Map<Class<?>, Object> irs;

  public TranslatorContext() {
    this.irs = new HashMap<>();
  }

  public <Ir> void addIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkFalse(irs.containsKey(ir.getClass()), "Ir already defined.");
    irs.put(ir.getClass(), ir);
  }

  @SuppressWarnings("unchecked")
  public <Ir> Ir getIr(final Class<Ir> irClass) {
    InvariantChecks.checkNotNull(irClass);
    InvariantChecks.checkTrue(irs.containsKey(irClass), "Ir undefined.");
    return (Ir) irs.get(irClass);
  }
}

