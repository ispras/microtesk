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

package ru.ispras.microtesk.docgen;

import java.io.IOException;
import ru.ispras.microtesk.translator.simnml.ir.IrVisitor;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Shortcut;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public class XmlDocumenter implements IrVisitor {


  private DocBook docBook;

  public XmlDocumenter(DocBook dump) throws IOException {
    this.docBook = dump;
  }

  @Override
  public void onResourcesBegin() {
    docBook.beginChapter("Resources", "resources", XmlScope.RESOURCES);
  }

  @Override
  public void onResourcesEnd() {
    docBook.closeChapter();
  }

  @Override
  public void onLetConstant(LetConstant let) {
    docBook.addParagraph(let.getName());
  }

  @Override
  public void onLetString(LetString let) {
    docBook.addParagraph(let.getName());
  }

  @Override
  public void onLetLabel(LetLabel let) {
    docBook.addParagraph(let.getName());
  }

  @Override
  public void onType(String name, Type type) {
    docBook.addParagraph(name);
  }

  @Override
  public void onMemory(String name, MemoryExpr memory) {
    docBook.addParagraph(name);
  }

  @Override
  public void onPrimitiveBegin(Primitive item) {
    docBook.addParagraph(item.getName());
  }

  @Override
  public void onPrimitiveEnd(Primitive item) {}

  @Override
  public void onAlternativeBegin(PrimitiveOR orRule, Primitive item) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAlternativeEnd(PrimitiveOR orRule, Primitive item) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onArgumentBegin(PrimitiveAND andRule, String argName, Primitive argType) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onArgumentEnd(PrimitiveAND andRule, String argName, Primitive argType) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAttributeBegin(PrimitiveAND andRule, Attribute attr) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAttributeEnd(PrimitiveAND andRule, Attribute attr) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatement(PrimitiveAND andRule, Attribute attr, Statement stmt) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onShortcutBegin(PrimitiveAND andRule, Shortcut shortcut) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onShortcutEnd(PrimitiveAND andRule, Shortcut shortcut) {
    // TODO Auto-generated method stub

  }

//  /**
//   * TODO: rewrite with XmlScopes.
//   * 
//   * @param scope: the scope that is intended to write.
//   */
//  @SuppressWarnings("unused")
//  @Deprecated
//  private void checkScope(String scope) {
//    if (currentScope.peek().getTag() != scope) {
//      try {
//        throw new Exception("Bad model structure");
//      } catch (Exception e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//    }
//  }

}
