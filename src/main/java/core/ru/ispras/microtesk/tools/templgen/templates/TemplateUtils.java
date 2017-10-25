/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;

public class TemplateUtils {

  public static String getImmValue() {
    
    
    
    return null;
  }

  public static String getPreparator() {

    return null;
  } 
  
  // TODO:
  public static boolean printMetaOperation(MetaOperation operation) {
    System.out.format("getTypeName: %s \n", operation.toString());
    System.out.format("Operation: %s \n", operation.getName());
    System.out.format("getTypeName: %s \n", operation.getTypeName());
    System.out.format("getDataType: %s \n", operation.getDataType());
    System.out.format("hasRootShortcuts: %s \n", operation.hasRootShortcuts());
    System.out.format("isBranch: %s \n", operation.isBranch());
    System.out.format("isConditionalBranch: %s \n", operation.isConditionalBranch());
    System.out.format("isLoad: %s \n", operation.isLoad());
    System.out.format("isRoot: %s \n", operation.isRoot());
    System.out.format("isStore: %s \n", operation.isStore());
    System.out.format("getTypeName: %s \n", operation);
    
    Iterable<MetaArgument> arguments = operation.getArguments();

    for (MetaArgument argument : arguments) {
      Collection<String> tempTypes = argument.getTypeNames();
      System.out.format("getName: %s \n", argument.getName());
      System.out.format("getMode: %s \n", argument.getMode());
      System.out.format("getDataType: %s \n", argument.getDataType());
      System.out.format("getKind: %s \n", argument.getKind());
      System.out.format("getTypeNames: %s \n", argument.getTypeNames());
      System.out.format("toString: %s \n\n", argument.toString());
    }
    
    System.out.format("\n");
    return false;
  }

  public static boolean isBranchOperation(MetaOperation operation) {
    String instructionName = operation.getName();
    if (instructionName.startsWith("b") || instructionName.startsWith("j")) return true;
    return false;
  }

  // TODO:
  public static int getArgumentsNumber(Iterable<MetaArgument> arguments) {
    if (arguments instanceof Collection<?>) {
      return ((Collection<?>)arguments).size();
    }
    return 0;
  }
}
