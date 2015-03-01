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

package ru.ispras.microtesk.translator.antlrex.log;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

/**
 * The {@code LogStoreListener} class is designed as a base class for classes that listen
 * to events posted to the log. Such classes are needed for unit testing when the test bench
 * checks whether the translator works correctly by monitoring errors it raises.    
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public abstract class LogStoreListener implements LogStore {
  private final LogStore log;

  public LogStoreListener(LogStore log) {
    checkNotNull(log);
    this.log = log;
  }

  @Override
  public final void append(LogEntry entry) {
    log.append(entry);
    check(entry);
  }

  protected abstract void check(LogEntry entry);
}
