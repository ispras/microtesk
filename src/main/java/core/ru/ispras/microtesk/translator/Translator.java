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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.translator.antlrex.log.LogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogStoreConsole;

public class Translator<Ir> {
  private final List<TranslatorHandler<Ir>> handlers = new ArrayList<>();
  private LogStore log;

  public Translator() {
    this.log = LogStoreConsole.INSTANCE;
  }

  public final LogStore getLog() {
    return log;
  }

  public final void setLog(LogStore log) {
    checkNotNull(log);
    this.log = log;
  }

  public final void addHandler(TranslatorHandler<Ir> handler) {
    checkNotNull(handler);
    handlers.add(handler);
  }

  public final void processIr(Ir ir) {
    for (TranslatorHandler<Ir> handler : handlers) {
      handler.processIr(ir);
    }
  }
}
