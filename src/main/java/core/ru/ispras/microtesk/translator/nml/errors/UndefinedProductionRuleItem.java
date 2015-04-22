/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.nml.ESymbolKind;

public class UndefinedProductionRuleItem implements ISemanticError {
  private static final String FORMAT =
    "The '%s' item of the '%s' %s-rule is not defined or is not a %s definition.";

  private final String itemName;
  private final String ruleName;
  private final boolean isOrRule;
  private final ESymbolKind expectedKind;

  public UndefinedProductionRuleItem(String itemName, String ruleName, boolean isOrRule,
      ESymbolKind expectedKind) {
    this.itemName = itemName;
    this.ruleName = ruleName;
    this.isOrRule = isOrRule;
    this.expectedKind = expectedKind;
  }

  @Override
  public String getMessage() {
    return String.format(FORMAT, itemName, ruleName, isOrRule ? "OR" : "AND", expectedKind.name());
  }
}
