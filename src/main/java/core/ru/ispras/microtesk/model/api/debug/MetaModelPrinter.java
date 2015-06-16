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

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;

public final class MetaModelPrinter {
  private final MetaModel metaModel;

  public MetaModelPrinter(MetaModel metaModel) {
    if (null == metaModel) {
      throw new NullPointerException();
    }

    this.metaModel = metaModel;
  }

  public void printAll() {
    printSepator();
    printRegisterMetaData();

    printSepator();
    printMemoryMetaData();

    printSepator();
    printAddressingModeMetaData();

    printSepator();
    printOperationMetaData();

  }

  public void printSepator() {
    System.out.println("************************************************");
  }

  public void printRegisterMetaData() {
    System.out.println("REGISTERS:");
    for (MetaLocationStore r : metaModel.getRegisters()) {
      System.out.printf("Name: %s, Size: %d%n", r.getName(), r.getCount());
    }
  }

  public void printMemoryMetaData() {
    System.out.println("MEMORY STORES:");
    for (MetaLocationStore m : metaModel.getMemoryStores()) {
      System.out.printf("Name: %s, Size: %d%n", m.getName(), m.getCount());
    }
  }

  private void printAddressingModeMetaData() {
    System.out.println("ADDRESSING MODE GROUPS:");
    boolean isEmpty = true;
    for (MetaGroup g : metaModel.getAddressingModeGroups()) {
      isEmpty = false;
      System.out.println("   " + g.getName());
      for (MetaData md : g.getItems()) {
        System.out.println("      " + md.getName());
      }
    }
    if (isEmpty) {
      System.out.println("   <NO>");
    }
    System.out.println();

    System.out.println("ADDRESSING MODES:");

    for (MetaAddressingMode am : metaModel.getAddressingModes()) {
      final StringBuilder sb = new StringBuilder();

      sb.append(String.format("Name: %s", am.getName()));
      sb.append(", Parameters: ");

      boolean isFirstArg = true;
      for (String an : am.getArgumentNames()) {
        if (isFirstArg) {
          isFirstArg = false;
        } else {
          sb.append(", ");
        }
        sb.append(an);
      }

      System.out.println(sb);
    }
  }

  private void printOperationMetaData() {
    System.out.println("OPERATION GROUPS:");
    boolean isEmpty = true;
    for (MetaGroup g : metaModel.getOperationGroups()) {
      isEmpty = false;
      System.out.println("   " + g.getName());
      for (MetaData md : g.getItems()) {
        System.out.println("      " + md.getName());
      }
    }
    if (isEmpty) {
      System.out.println("   <NO>");
    }
    System.out.println();

    System.out.println("OPERATIONS:");
    for (MetaOperation o : metaModel.getOperations()) {
      System.out.println(String.format("Name: %s", o.getName()));
      System.out.println("Parameters:");

      int count = 0;
      for (MetaArgument a : o.getArguments()) {
        printArgument(a);
        count++;
      }

      if (0 == count) {
        System.out.println("   <none>");
      }

      System.out.println("Shortcuts:");

      count = 0;
      for (MetaShortcut s : o.getShortcuts()) {
        printShortcut(s);
        count++;
      }

      if (0 == count) {
        System.out.println("   <none>");
      }

      System.out.println();
    }
  }

  private void printArgument(MetaArgument a) {
    final StringBuilder asb = new StringBuilder();

    asb.append("   ");
    asb.append(a.getName());
    asb.append(" [");

    boolean isFirstMode = true;
    for (String tn : a.getTypeNames()) {
      if (isFirstMode) {
        isFirstMode = false;
      } else {
        asb.append(", ");
      }
      asb.append(tn);
    }

    asb.append("]");
    System.out.println(asb);
  }

  private void printShortcut(MetaShortcut s) {
    System.out.printf("   Name: %s%n", s.getOperation().getName());
    System.out.printf("   Context: %s%n", s.getContextName());
    System.out.println("   Parameters:");

    int count = 0;
    for (MetaArgument a : s.getOperation().getArguments()) {
      System.out.print("   ");
      printArgument(a);
      count++;
    }
    if (0 == count) {
      System.out.println("   <none>");
    }
  }
}
