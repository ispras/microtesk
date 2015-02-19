package ru.ispras.microtesk.docgen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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

/**
 * TODO: Eliminate code duplication in methods.
 * 
 * @author Platon
 *
 */
public class XmlDocumenter implements IrVisitor {


  private DocBook xml;
  private Stack<XML> currentScope;

  public XmlDocumenter(DocBook xml, String modelName) throws IOException {
    this.xml = xml;
    currentScope = new Stack<XML>();
  }

  @Override
  public void onResourcesBegin() {
    try {
      currentScope.push(xml.addChapter("Resources", "resources", XmlScope.RESOURCES));

    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onResourcesEnd() {
    currentScope.pop();
  }

  @Override
  public void onLetConstant(LetConstant let) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "let_constant");
    XML letConstant = new XML("para", XmlElementType.LEAF, attr);
    try {
      letConstant.assignContent(let.getName());
      currentScope.peek().addSubEntry(letConstant);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onLetString(LetString let) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "let_string");
    XML letString = new XML("para", XmlElementType.LEAF, attr);
    try {
      letString.assignContent(let.getName());
      currentScope.peek().addSubEntry(letString);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onLetLabel(LetLabel let) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "let_label");
    XML letLabel = new XML("para", XmlElementType.LEAF, attr);
    try {
      letLabel.assignContent(let.getName());
      currentScope.peek().addSubEntry(letLabel);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onType(String name, Type type) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "type_declaration");
    XML pr_type = new XML("para", XmlElementType.LEAF, attr);
    try {
      pr_type.assignContent(name);
      currentScope.peek().addSubEntry(pr_type);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onMemory(String name, MemoryExpr memory) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "memory_description");
    XML mem = new XML("para", XmlElementType.LEAF, attr);
    try {
      mem.assignContent(name);
      currentScope.peek().addSubEntry(mem);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onPrimitiveBegin(Primitive item) {
    Map<String, String> attr = new HashMap<>();
    attr.put("entity", "primitive");
    XML primitive = new XML("para", XmlElementType.LEAF, attr);
    try {
      if (currentScope.isEmpty() || currentScope.peek().getScope() != XmlScope.PRIMITIVES) {
        currentScope.push(xml.addChapter("Primitives", "primitives", XmlScope.PRIMITIVES));
      }

      primitive.assignContent(item.getName());
      currentScope.peek().addSubEntry(primitive);
    } catch (FormatterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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

  /**
   * TODO: rewrite with XmlScopes.
   * 
   * @param scope: the scope that is intended to write.
   */
  @SuppressWarnings("unused")
  @Deprecated
  private void checkScope(String scope) {
    if (currentScope.peek().getTag() != scope) {
      try {
        throw new Exception("Bad model structure");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
