/*
 * Copyright 2018-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.templgen.templates;

import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaOperation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * {@link TemplatesUtils} contains a number of utilities to deal with autogen Templates.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public final class TemplatesUtils {
  public static void printMetaOperation(final MetaOperation operation) {
    FileWriter file = null;
    try {
      file = new FileWriter("autogen_log.txt", true);
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    PrintWriter printWriter;
    printWriter = new PrintWriter(new BufferedWriter(file));

    //if (operation.getName() == "mfc0") {
    printWriter.format("getTypeName: %s \n", operation.toString());
    printWriter.format("Operation: %s \n", operation.getName());
    printWriter.format("getTypeName: %s \n", operation.getTypeName());
    printWriter.format("getDataType: %s \n", operation.getDataType());
    printWriter.format("hasRootShortcuts: %s \n", operation.hasRootShortcuts());
    printWriter.format("isBranch: %s \n", operation.isBranch());
    printWriter.format("isConditionalBranch: %s \n", operation.isConditionalBranch());
    printWriter.format("isLoad: %s \n", operation.isLoad());
    printWriter.format("isRoot: %s \n", operation.isRoot());
    printWriter.format("isStore: %s \n", operation.isStore());
    printWriter.format("getTypeName: %s \n", operation);

    Iterable<MetaArgument> arguments = operation.getArguments();

    for (MetaArgument argument : arguments) {
      printWriter.format("getName: %s \n", argument.getName());
      printWriter.format("getMode: %s \n", argument.getMode());
      printWriter.format("getDataType: %s \n", argument.getDataType());
      printWriter.format("getKind: %s \n", argument.getKind());
      printWriter.format("getTypeNames: %s \n", argument.getTypeNames());
      // System.out.println(argument.getTypeNames().toString().toLowerCase());
      printWriter.format("toString: %s \n\n", argument.toString());
    }

    printWriter.format("\n");

    printWriter.close();
  }
}
