/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

import java.io.FileWriter;
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

public class TexVisitor implements IrVisitor {


  private FileWriter writer;
  private int level = 0;
  private boolean isShortcut = false;

  public TexVisitor(FileWriter writer) throws IOException {
    this.writer = writer;
    writer.write("\\documentclass{book}\n");
    writer.write("\\begin{document}");
  }

  @Override
  public void onResourcesBegin() {
    try {
      level = 1;
      setToLevel(level++);
      writer.write("\\chapter{Resources}");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onResourcesEnd() {
    // tex.closeChapter();
  }

  @Override
  public void onLetConstant(LetConstant let) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(let.getName()) + " }}");
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onLetString(LetString let) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(let.getName()) + " }}");
      setToLevel(level);
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onLetLabel(LetLabel let) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(let.getName()) + " }}");
      setToLevel(level);
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onType(String name, Type type) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(name) + " }}");
      setToLevel(level);
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onMemory(String name, MemoryExpr memory) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(name) + " }}");
      setToLevel(level);
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onPrimitiveBegin(Primitive item) {
    try {
      setToLevel(level);
      writer.write("\\section{\\texttt{ " + makeEscapeChars(item.getName()) + " }}");
      setToLevel(++level);

      if ((item instanceof PrimitiveAND && ((PrimitiveAND) item).getArguments().size() > 0)
          && !isShortcut) {
        if (item.getName().contains("mips_break")) {
          System.out.println();
        }
        writer.write("\\textbf{" + makeEscapeChars(item.getName()) + "} is an operation. ");
        writer.write("It takes the following arguments:\n");
        setToLevel(level);
        writer.write("\\begin{itemize}");
        level++;
      }

      if (item instanceof PrimitiveOR && ((PrimitiveOR) item).getORs().size() > 0) {
        writer.write("\\textbf{" + makeEscapeChars(item.getName())
            + "} is an alternative between the following primitives:\n");
        setToLevel(level);
        writer.write("\\begin{itemize}");
        level++;
      }


    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onPrimitiveEnd(Primitive item) {
    try {
      if (((item instanceof PrimitiveAND && ((PrimitiveAND) item).getArguments().size() > 0) || (item instanceof PrimitiveOR && ((PrimitiveOR) item)
          .getORs().size() > 0)) && !isShortcut) {
        setToLevel(--level);
        writer.write("\\end{itemize}");
      }
      setToLevel(--level);
      writer.write("\\newpage");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onAlternativeBegin(PrimitiveOR orRule, Primitive item) {
    try {

      writer.write("\n\\item \\textbf{" + makeEscapeChars(item.getName()) + "}: "
          + makeEscapeChars(item.getName()));

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void onAlternativeEnd(PrimitiveOR orRule, Primitive item) {

  }

  @Override
  public void onArgumentBegin(PrimitiveAND andRule, String argName, Primitive argType) {
    if (!isShortcut) {
      try {
        setToLevel(level);
        writer.write("\\item \\textbf{" + makeEscapeChars(argType.getName()) + "}: "
            + makeEscapeChars(argName));

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
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
    isShortcut = true;

  }

  @Override
  public void onShortcutEnd(PrimitiveAND andRule, Shortcut shortcut) {
    isShortcut = false;

  }

  @Override
  public void onPrimitivesBegin() {
    try {
      level = 1;
      setToLevel(level++);
      writer.write("\\chapter{Primitives}");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onPrimitivesEnd() {

  }

  private void setToLevel(int level) throws IOException {
    writer.write('\r');
    for (int i = 0; i < level; i++) {
      writer.write('\t');
    }
  }

  public void finalize() {

    try {
      setToLevel(0);
      writer.write("\\end{document}");
      writer.flush();
      writer.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private String makeEscapeChars(String s) {
    return s.replace("\\", "\\\\").replace("_", "\\_").replace("$", "\\$").replace("%", "\\%")
        .replace("#", "\\#").replace("{", "\\{").replace("}", "\\}").replace("~", "\\~")
        .replace("^", "\\^");
  }
}
