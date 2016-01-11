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

package ru.ispras.microtesk.model.api.metadata;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;

public final class MetaModelPrinter {
  private final MetaModel metaModel;

  public MetaModelPrinter(final MetaModel metaModel) {
    InvariantChecks.checkNotNull(metaModel);
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
    Logger.message(Logger.BAR);
  }

  public void printRegisterMetaData() {
    Logger.message("REGISTERS:");
    for (final MetaLocationStore r : metaModel.getRegisters()) {
      Logger.message("Name: %s, Size: %d", r.getName(), r.getCount());
    }
  }

  public void printMemoryMetaData() {
    Logger.message("MEMORY STORES:");
    for (final MetaLocationStore m : metaModel.getMemoryStores()) {
      Logger.message("Name: %s, Size: %d", m.getName(), m.getCount());
    }
  }

  private void printAddressingModeMetaData() {
    Logger.message("ADDRESSING MODE GROUPS:");
    boolean isEmpty = true;
    for (final MetaGroup g : metaModel.getAddressingModeGroups()) {
      isEmpty = false;
      Logger.message("   " + g.getName());
      for (final MetaData md : g.getItems()) {
        Logger.message("      " + md.getName());
      }
    }
    if (isEmpty) {
      Logger.message("   <NO>");
    }
    Logger.message("");

    Logger.message("ADDRESSING MODES:");
    for (final MetaAddressingMode am : metaModel.getAddressingModes()) {
      final StringBuilder sb = new StringBuilder();

      sb.append(String.format("Name: %s", am.getName()));
      if (am.canThrowException()) {
        sb.append(" throws");
      }

      sb.append(", Parameters: ");

      boolean isFirstArg = true;
      for (final String an : am.getArgumentNames()) {
        if (isFirstArg) {
          isFirstArg = false;
        } else {
          sb.append(", ");
        }
        sb.append(an);
      }

      Logger.message(sb.toString());
    }
  }

  private void printOperationMetaData() {
    Logger.message("OPERATION GROUPS:");
    boolean isEmpty = true;
    for (final MetaGroup g : metaModel.getOperationGroups()) {
      isEmpty = false;
      Logger.message("   " + g.getName());
      for (final MetaData md : g.getItems()) {
        Logger.message("      " + md.getName());
      }
    }
    if (isEmpty) {
      Logger.message("   <NO>");
    }
    Logger.message("");

    Logger.message("OPERATIONS:");
    for (final MetaOperation o : metaModel.getOperations()) {
      Logger.message(String.format(
          "Name: %s%s", o.getName(), o.canThrowException() ? " throws" : ""));
      Logger.message("Parameters:");

      int count = 0;
      for (final MetaArgument a : o.getArguments()) {
        printArgument(a, "");
        count++;
      }

      if (0 == count) {
        Logger.message("   <none>");
      }

      Logger.message("Shortcuts:");

      count = 0;
      for (final MetaShortcut s : o.getShortcuts()) {
        printShortcut(s);
        count++;
      }

      if (0 == count) {
        Logger.message("   <none>");
      }

      Logger.message("");
    }
  }

  private void printArgument(final MetaArgument a, final String indent) {
    final StringBuilder asb = new StringBuilder();

    asb.append(indent);
    asb.append("   ");
    asb.append(a.getName());
    asb.append(" [");

    boolean isFirstMode = true;
    for (final String tn : a.getTypeNames()) {
      if (isFirstMode) {
        isFirstMode = false;
      } else {
        asb.append(", ");
      }
      asb.append(tn);
    }

    asb.append("]");
    Logger.message(asb.toString());
  }

  private void printShortcut(final MetaShortcut s) {
    Logger.message("   Name: %s%s",
        s.getOperation().getName(), s.getOperation().canThrowException() ? " throws" : "");

    Logger.message("   Context: %s", s.getContextName());
    Logger.message("   Parameters:");

    int count = 0;
    for (final MetaArgument a : s.getOperation().getArguments()) {
      printArgument(a, "   ");
      count++;
    }
    if (0 == count) {
      Logger.message("   <none>");
    }
  }
}
