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

package ru.ispras.microtesk.translator.simnml.ir;

import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public interface IRVisitor {
  void onResourcesBegin();
  void onResourcesEnd();

  void onLetConstant(String name, LetConstant value);
  void onLetString(String name, LetString value);
  void onLetLabel(String name, LetLabel value);

  void onType(String name, Type value);
  void onMemory(String name, MemoryExpr value);

  void onPrimitiveBegin(String name, Primitive value);
  void onPrimitiveEnd(String name, Primitive value);

  void onAlternativeBegin(Primitive value);
  void onAlternativeEnd(Primitive value);

  void onArgumentBegin(String name, Primitive value);
  void onArgumentEnd(String name, Primitive value);

  void onAttributeBegin(Attribute attribute);
  void onAttributeEnd(Attribute attribute);
  void onStatement(Statement stmt);

  void onShortcutBegin(
      String name,
      List<String> contexts,
      PrimitiveAND entry,
      PrimitiveAND target);

  void onShortcutEnd(
      String name,
      List<String> contexts,
      PrimitiveAND entry,
      PrimitiveAND target);
}
