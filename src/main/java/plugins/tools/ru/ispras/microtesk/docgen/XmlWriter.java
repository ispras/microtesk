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
    if (xml.getType() == XmlElementType.INTERMEDIATE) {
      writer.write("<" + xml.getTag());
      if (xml.getAttributes() != null) {
        for (Map.Entry<String, String> entry : xml.getAttributes().entrySet()) {
          writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\" ");
        }
      }

      writer.write(">");

      level++;

      for (XML sub : xml.getSubXmls()) {
        setToCurrentLevel(level);
        write(sub);
      }

      setToCurrentLevel(--level);

      writer.write("</" + xml.getTag() + ">");

    } else {
      writer.write("<" + xml.getTag());
      if (xml.getAttributes() != null) {
        for (Map.Entry<String, String> entry : xml.getAttributes().entrySet()) {
          writer.write(" " + entry.getKey() + "=\"" + entry.getValue() + "\" ");
        }
      }
      writer.write(">" + xml.getContent() + "</" + xml.getTag() + ">\n");
    }
  }

  private void setToCurrentLevel(int level) throws IOException {
    writer.write('\r');
    for (int i = 0; i < level; i++) {
      writer.write('\t');
    }
  }
}
