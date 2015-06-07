/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.debug;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.memory.LocationAccessor;

public final class ModelStatePrinter {
  private final IModel model;

  public ModelStatePrinter(IModel model) {
    if (null == model) {
      throw new NullPointerException();
    }

    this.model = model;
  }

  public void printAll() {
    printSepator();
    System.out.println("MODEL STATE:");

    printRegisters();
    printMemory();

    printSepator();
  }

  public void printSepator() {
    System.out.println("************************************************");
  }

  public void printRegisters() {
    printSepator();

    System.out.println("REGISTER STATE:");
    System.out.println();

    final IModelStateObserver observer = model.getStateObserver();
    for (MetaLocationStore r : model.getMetaData().getRegisters()) {
      for (int index = 0; index < r.getCount().intValue(); ++index) {
        try {
          final LocationAccessor location = observer.accessLocation(r.getName(), index);

          if (1 == r.getCount().intValue()) {
            System.out.printf("%s = %s %n", r.getName(), location.toBinString());
          } else {
            System.out.printf("%s[%d] = %s %n", r.getName(), index, location.toBinString());
          }
        } catch (ConfigurationException e) {
          e.printStackTrace();
        }
      }
      System.out.println();
    }
  }

  public void printMemory() {
    printSepator();

    System.out.println("MEMORY STATE:");
    System.out.println();

    final IModelStateObserver observer = model.getStateObserver();
    for (MetaLocationStore r : model.getMetaData().getMemoryStores()) {
      for (int index = 0; index < r.getCount().intValue(); ++index) {
        try {
          final LocationAccessor location = observer.accessLocation(r.getName(), index);
          System.out.printf("%s[%d] = %s %n", r.getName(), index, location.toBinString());
        } catch (ConfigurationException e) {
          e.printStackTrace();
        }
      }
      System.out.println();
    }
  }
}
