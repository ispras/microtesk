/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.state;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;

public final class ModelStatePrinter {
  private final IModel model;

  public ModelStatePrinter(final IModel model) {
    InvariantChecks.checkNotNull(model);
    this.model = model;
  }

  public void printAll() {
    printSepator();
    Logger.message("MODEL STATE:");
    Logger.message("");

    printRegisters();
    printMemory();

    printSepator();
  }

  public void printSepator() {
    Logger.message(Logger.BAR);
  }

  public void printRegisters() {
    printSepator();
    Logger.message("REGISTER STATE:");
    Logger.message("");

    final IModelStateObserver observer = model.getStateObserver();
    for (MetaLocationStore r : model.getMetaData().getRegisters()) {
      for (BigInteger index = BigInteger.ZERO; index.compareTo(r.getCount()) < 0; index = index.add(BigInteger.ONE)) {
        try {
          final LocationAccessor location = observer.accessLocation(r.getName(), index);

          if (1 == r.getCount().intValue()) {
            Logger.message("%s = %s", r.getName(), location.toBinString());
          } else {
            Logger.message("%s[%d] = %s", r.getName(), index, location.toBinString());
          }
        } catch (ConfigurationException e) {
          e.printStackTrace();
        }
      }
      Logger.message("");
    }
  }

  public void printMemory() {
    printSepator();

    Logger.message("MEMORY STATE:");
    Logger.message("");

    final IModelStateObserver observer = model.getStateObserver();
    for (final MetaLocationStore r : model.getMetaData().getMemoryStores()) {
      for (BigInteger index = BigInteger.ZERO; index.compareTo(r.getCount()) < 0; index = index.add(BigInteger.ONE)) {
        try {
          final LocationAccessor location = observer.accessLocation(r.getName(), index);
          Logger.message("%s[%d] = %s", r.getName(), index, location.toBinString());
        } catch (final ConfigurationException e) {
          e.printStackTrace();
        }
      }
      Logger.message("");
    }
  }
}
