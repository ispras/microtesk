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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class XmlWriter {

  private FileWriter writer;
  private int level = 0;

  public XmlWriter(FileWriter writer) throws IOException {
    this.writer = writer;
    writer.write("<?xml version=\"1.0\" encoding=\"unicode\"?>\n");
  }

  public void close() throws IOException {
    writer.flush();
    writer.close();
  }

  public void write(XML xml) throws IOException {
    if (!xml.getSubXmls().isEmpty()) {
      writer.write("<" + xml.getTag());
      if (xml.getAttributes() != null) {
        for (Map.Entry<String, String> entry : xml.getAttributes().entrySet()) {
          writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\" ");
        }
      }

      writer.write("> ");
      if (xml.getContent() != null)
      {
        writer.write(xml.getContent());
      }

      level++;

      for (XML sub : xml.getSubXmls()) {
        setToCurrentLevel(level);
        write(sub);
      }

      setToCurrentLevel(--level);


      writer.write(" </" + xml.getTag() + ">");

    } else {
      writer.write("<" + xml.getTag());
      if (xml.getAttributes() != null) {
        for (Map.Entry<String, String> entry : xml.getAttributes().entrySet()) {
          writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
        }
      }
      writer.write("> " + xml.getContent() + " </" + xml.getTag() + ">\n");
    }
  }

  private void setToCurrentLevel(int level) throws IOException {
    writer.write('\r');
    for (int i = 0; i < level; i++) {
      writer.write('\t');
    }
  }
}
